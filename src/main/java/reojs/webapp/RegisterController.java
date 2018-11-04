package reojs.webapp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import reojs.system.JudgeFailure;


@Controller
@RequestMapping("/register")
public class RegisterController {
    private static final Log log = LogFactory.getLog(RegisterController.class);

    @Autowired
    private RegisterService service;


    @PostMapping()
	public void register(HttpServletRequest request, HttpServletResponse response)
                         throws IOException {
        log(request);
        if (!service.isValid(request)) {
            log.warn("Bad request from " + request.getRemoteAddr());
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            var ticket = service.createTicket(request);
            request.getServletContext().setAttribute("ticket"+ticket.getId(), ticket);

            var data = new JSONObject().put("name", "ticket_id")
                                       .put("value", ticket.getId());
            response.getWriter().append(data.toString()).flush();
        } catch (JudgeFailure e) {
            var data = new JSONObject().put("name", "error")
                                       .put("code", e.getErrorCode())
                                       .put("message", e.getMessage());
            response.getWriter().append(data.toString()).flush();
        } catch (WebappException e) {
            log.error(e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
	}

    private void log(HttpServletRequest request) {
        if (log.isTraceEnabled()) {
            StringBuilder s = new StringBuilder("Request{");
            for (var x : request.getParameterMap().entrySet()) {
                String fieldName = x.getKey();
                String value = Arrays.stream(x.getValue())
                        .map(v -> (v.length() < 10) ? v : "...")
                        .collect(Collectors.joining("; "));
                s.append(String.format("%s: [%s]; ", fieldName, value));
            }
            log.trace(s.append("}"));
        }
    }
}
