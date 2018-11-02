package reojs.webapp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Consumer;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import reojs.JudgeFailure;
import reojs.system.core.JudgeReport;
import reojs.system.core.JudgeSystem;
import reojs.system.core.Ticket;


@WebServlet("/judge")
public class JudgeService extends HttpServlet {
    private static final Log log = LogFactory.getLog(JudgeService.class);


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
                         throws IOException {
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");

        Ticket ticket;
        try {
            ticket = getTicket(request.getParameter("ticket_id"));
        } catch (Exception e) {
            log.warn(String.format("%s (%s)", e.getMessage(), request.getRemoteAddr()));
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // By default HTML5 server-sent event sends GET request to server every three seconds and
        // we ignore the request for the same judgement.
        var session = request.getSession();
        if (session.getAttribute(ticket.getId()) != null) {
            log.trace("Ignore the same request.");
            return;
        }
        session.setAttribute(ticket.getId(), ticket.getId());

        Writer writer = new Writer(response);
        ticket.getJudgement().addProgressListener((progress, status) ->
                writer.accept(new JSONObject().put("name", "progress")
                                      .put("progress", progress)
                                      .put("status", status).toString()));
        try {
            var judgeReport = ticket.submit().orElse(null);
            if (judgeReport == null) {
                throw new Exception("System error.");
            }
            sendJudgeReport(writer, judgeReport);
        } catch (JudgeFailure e) {
            sendErrorMessage(writer, e);
        } catch (Exception e) {
            log.error("Failed to complete a judgement.", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            session.removeAttribute(ticket.getId());
            getServletContext().removeAttribute("ticket"+ticket.getId());
        }
    }

    private Ticket getTicket(String id) throws Exception {
        if (id == null) {
            throw new Exception("The request query 'ticket_id' is absent.");
        }
        var ticket = (Ticket)getServletContext().getAttribute("ticket"+id);
        if (ticket == null) {
            throw new Exception(String.format("The ticket id '%s' does not exits.", id));
        }
        return ticket;
    }

    private void sendErrorMessage(Writer w, JudgeFailure e) {
        var data = new JSONObject().put("name", "error")
                                   .put("code", e.getErrorCode())
                                   .put("message", e.getMessage());
        w.accept(data.toString());
    }

    private void sendJudgeReport(Writer w, JudgeReport report) {
        var judgement = report.getJudgement();
        var submission = judgement.getSubmission();
        var base = JudgeSystem.getConfig().getInteger("system.base_score");

        var results = new JSONArray();
        for (var result : report.getJudgeResults()) {
            results.put(new JSONObject()
                            .put("passed", result.isPassed())
                            .put("timedout", result.isTimedout())
                            .put("input", new JSONArray(result.getTestCase().getInputs()))
                            .put("answer", result.getAnswer())
                            .put("expected", result.getTestCase().getOutput()));
        }

        var data = new JSONObject()
                        .put("name", "report")
                        .put("problem_id", submission.getProblemId())
                        .put("num_passed", report.getNumPassed())
                        .put("num_testcases", report.getJudgeResults().size())
                        .put("score", report.getScore(base))
                        .put("runtime", report.getElapsedTime())
                        .put("results", results);

        w.accept(data.toString());
    }
}


class Writer implements Consumer<String> {
    private HttpServletResponse response;

    Writer(HttpServletResponse r) {
        this.response = r;
    }

    @Override
    public void accept(String s) {
        try {
            response.getWriter().append("data: ").append(s).append("\n\n").flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
