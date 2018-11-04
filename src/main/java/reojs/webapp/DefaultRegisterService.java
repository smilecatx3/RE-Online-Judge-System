package reojs.webapp;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;

import reojs.system.JudgeSystemException;
import reojs.system.Submission;
import reojs.system.core.JudgeSystem;
import reojs.system.core.Ticket;


@Service
public class DefaultRegisterService implements RegisterService {
    @Autowired
    private MultipartResolver resolver;


    @Override
    public boolean isValid(HttpServletRequest request) {
        if (!resolver.isMultipart(request)) {
            return false;
        }

        var multipartRequest = (MultipartHttpServletRequest)request;
        Function<String, Boolean> checker1 = multipartRequest.getParameterMap()::containsKey;
        Function<String, Boolean> checker2 = multipartRequest.getFileMap()::containsKey;
        return checker1.apply("problem_id") && checker1.apply("user_id") &&
               checker1.apply("language") &&
               (checker1.apply("source_text") ^ checker2.apply("source_file"));
    }

    @Override
    public Path upload(HttpServletRequest request) throws WebappException {
        MultipartFile file = ((MultipartHttpServletRequest)request).getFile("source_file");
        assert !Objects.isNull(file); // should has been validated in isValid()
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Empty source file.");
        }

        try {
            var workingDir = JudgeSystem.getConfig().getString("system.working_dir");
            String filename = FilenameUtils.removeExtension(file.getOriginalFilename())+"+";
            Path target = Files.createTempFile(Paths.get(workingDir), filename, ".zip");
            Files.write(target, file.getBytes());
            return target;
        } catch (IOException e) {
            throw new WebappException("Failed to upload file.", e);
        }
    }

    @Override
    public Ticket createTicket(HttpServletRequest request) throws WebappException {
        var source = new JSONObject().put("language", request.getParameter("language"));
        if (request.getParameterMap().containsKey("source_text")) {
            source.put("text", request.getParameter("source_text"));
        } else {
            source.put("file", upload(request).toString());
        }

        try {
            var submission = new Submission(new JSONObject()
                    .put("problem_id", request.getParameter("problem_id"))
                    .put("user_id", request.getParameter("user_id"))
                    .put("source", source));

            var ticket = JudgeSystem.newTicket(submission);
            if (ticket.isPresent()) {
                return ticket.get();
            } else {
                throw new WebappException("Failed to create a ticket.");
            }
        } catch (JudgeSystemException e) {
            throw new WebappException("Failed to create a submission instance.", e);
        }
    }
}
