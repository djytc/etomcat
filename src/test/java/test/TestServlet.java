package test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * User: alexey
 * Date: 8/28/11
 */
public class TestServlet extends HttpServlet {
    private static final long serialVersionUID = -8124821589871278803L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.getOutputStream().write(TestServlet.class.getSimpleName().getBytes("UTF-8"));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String param = req.getParameter("foo");
        boolean res = "bar".equals(param);
        resp.getOutputStream().write(Boolean.toString(res).getBytes("UTF-8"));
    }
}
