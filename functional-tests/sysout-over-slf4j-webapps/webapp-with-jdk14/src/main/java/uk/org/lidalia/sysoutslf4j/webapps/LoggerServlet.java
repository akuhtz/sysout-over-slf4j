package uk.org.lidalia.sysoutslf4j.webapps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.google.common.base.Optional.fromNullable;

public class LoggerServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String loggerName = fromNullable(request.getParameter("logger")).or(Logger.ROOT_LOGGER_NAME);
        boolean useNative = Boolean.valueOf(request.getParameter("native"));
        String message = request.getParameter("message");
        if (useNative) {
            java.util.logging.Logger.getLogger(loggerName).info(message);
        } else {
            LoggerFactory.getLogger(loggerName).info(message);
        }
    }
}