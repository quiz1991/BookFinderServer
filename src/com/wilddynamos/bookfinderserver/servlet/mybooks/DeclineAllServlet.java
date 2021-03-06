package com.wilddynamos.bookfinderserver.servlet.mybooks;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.*;
import javax.servlet.http.*;

import com.wilddynamos.bookfinderserver.model.Request;
import com.wilddynamos.bookfinderserver.service.RequestManager;
import com.wilddynamos.bookfinderserver.servlet.ActiveUserPool;

public class DeclineAllServlet extends HttpServlet {

	private static final long serialVersionUID = -8317612664338089488L;

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html");

		int result = -1;
		String bookId = request.getParameter("id");

		RequestManager rm = new RequestManager();
		List<Request> requesters = rm.findByProp("book_id", bookId,
				"request_time", null, Integer.MAX_VALUE, 1);

		for (Request r : requesters) {
			r.setStatus(false);
		}


		for (Request r : requesters) {
			result = rm.update(r);
		}

		rm.close();

		synchronized (ActiveUserPool.userId2bookIds) {
			for (Request r : requesters) {
				if (ActiveUserPool.userId2bookIds.get(r.getRequesterId()) == null)
					ActiveUserPool.userId2bookIds.put(r.getRequesterId(),
							new ArrayList<Integer>());
				ActiveUserPool.userId2bookIds.get(r.getRequesterId()).add(
						-Integer.parseInt(bookId));
			}

			ActiveUserPool.userId2bookIds.notifyAll();
		}

		response.getWriter().println(String.valueOf(result));
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		doGet(request, response);
	}
}
