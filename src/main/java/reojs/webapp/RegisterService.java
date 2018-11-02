package reojs.webapp;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import reojs.JudgeFailure;
import reojs.Submission;
import reojs.system.core.JudgeSystem;
import reojs.system.core.Ticket;


@WebServlet("/register")
public class RegisterService extends HttpServlet {
    private static final Log log = LogFactory.getLog(RegisterService.class);


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
                         throws IOException {
        // The servlet only handles POST requests.
        log.warn("GET request from " + request.getRemoteAddr());
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    @Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
                          throws IOException {
        Map<String, FileItem> postParams;
        try {
            postParams = parsePostRequest(request);
        } catch (FileUploadException e) {
            log.error("Failed to parse post request.", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        } catch (IllegalArgumentException e) {
            // Some required parameters are absent.
            log.warn("Bad request from " + request.getRemoteAddr());
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            var ticket = createTicket(postParams).orElse(null);
            if (ticket == null) {
                throw new Exception("Null ticket");
            }
            getServletContext().setAttribute("ticket"+ticket.getId(), ticket);

            var data = new JSONObject().put("name", "ticket_id")
                                       .put("value", ticket.getId());
            response.getWriter().append(data.toString()).flush();
        } catch (JudgeFailure e) {
            var data = new JSONObject().put("name", "error")
                                       .put("code", e.getErrorCode())
                                       .put("message", e.getMessage());
            response.getWriter().append(data.toString()).flush();
        } catch (Exception e) {
            log.error("Failed to create a ticket.", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
	}

	private Map<String, FileItem> parsePostRequest(HttpServletRequest request)
                                                   throws FileUploadException {
        var postParams = new ServletFileUpload(new DiskFileItemFactory())
                .parseRequest(request)
                .stream()
                .collect(Collectors.toMap(FileItem::getFieldName, item->item));
        log(postParams);

        Function<String, Boolean> checker = postParams::containsKey;
        boolean isValid = checker.apply("problem_id") && checker.apply("user_id") &&
                          checker.apply("language") &&
                          (checker.apply("source_text") ^ checker.apply("source_file"));
        if (!isValid) {
            throw new IllegalArgumentException();
        }

        return postParams;
    }

    private void log(Map<String, FileItem> postParams) {
        if (log.isTraceEnabled()) {
            StringBuilder s = new StringBuilder("Request{");
            for (var x : postParams.entrySet()) {
                String fieldName = x.getKey();
                String content = x.getValue().getString();
                content = (content.length() < 10) ? content : "...";
                s.append(String.format("%s: %s; ", fieldName, content));
            }
            log.trace(s.append("}"));
        }
    }

    private Optional<Ticket> createTicket(Map<String, FileItem> postParams) throws Exception {
        var source = new JSONObject().put("language", postParams.get("language").getString());
        if (postParams.containsKey("source_text")) {
            source.put("text", postParams.get("source_text").getString());
        } else {
            var workingDir = JudgeSystem.getConfig().getString("system.working_dir");
            FileItem sourceFile = postParams.get("source_file");
            String filename = FilenameUtils.removeExtension(sourceFile.getName())+"+";
            Path path = Files.createTempFile(Paths.get(workingDir), filename, ".zip");
            sourceFile.write(path.toFile());
            log.trace("Uploaded source file to " + path);
            source.put("file", path.toString());
        }

        var submission = new Submission(new JSONObject()
                .put("problem_id", postParams.get("problem_id").getString())
                .put("user_id", postParams.get("user_id").getString())
                .put("source", source));

        return JudgeSystem.newTicket(submission);
    }
}
