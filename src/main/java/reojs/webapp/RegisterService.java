package reojs.webapp;

import java.nio.file.Path;

import javax.servlet.http.HttpServletRequest;

import reojs.system.core.Ticket;


public interface RegisterService {
    /**
     * Checks whether the request is valid or not. In specific, the request should contain the
     * required parameters for creating a judge system ticket.
     */
    boolean isValid(HttpServletRequest request);

    /**
     * Uploads the source file to server if the submission is s zip file.
     *
     * @return the path to the uploaded file.
     */
    Path upload(HttpServletRequest request) throws WebappException;

    /**
     * @return a judge system ticket (never be null).
     * @throws WebappException if the system cannot create a ticket.
     */
    Ticket createTicket(HttpServletRequest request) throws WebappException;
}
