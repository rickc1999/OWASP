package servlets.admin.cheatSheet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Encoder;

import dbProcs.Getter;

import utils.CheatSheetStatus;
import utils.ExposedServer;
import utils.Validate;

/**
 * Control class responseable for returning a cheat sheet for a module
 * <br/><br/>
 * This file is part of the Security Shepherd Project.
 * 
 * The Security Shepherd project is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.<br/>
 * 
 * The Security Shepherd project is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.<br/>
 * 
 * You should have received a copy of the GNU General Public License
 * along with the Security Shepherd project.  If not, see <http://www.gnu.org/licenses/>. 
 * @author Mark Denihan
 *
 */
public class GetCheat extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(GetCheat.class);
	
	/**
	 * This method will reject requests if cheat sheet availability is marked as unavailable by administration.
	 * @param moduleId
	 * @param csrfToken
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		log.debug("*** servlets.Admin.GetCheat ***");
		String[] result = null;
		PrintWriter out = response.getWriter();  
		out.print(getServletInfo());
		Encoder encoder = ESAPI.encoder();
		HttpSession ses = request.getSession(true);
		if(Validate.validateSession(ses))
		{
			Cookie tokenCookie = Validate.getToken(request.getCookies());
			Object tokenParmeter = request.getParameter("csrfToken");
			if(Validate.validateTokens(tokenCookie, tokenParmeter))
			{
				if(CheatSheetStatus.getStatus())
				{
					String ApplicationRoot = getServletContext().getRealPath("");
					String moduleId = request.getParameter("moduleId");
					log.debug(ses.getAttribute("userName") + " submitted the following moduleId: " + moduleId);
					if(moduleId != null)
					{
						result = Getter.getModuleSolution(ApplicationRoot, moduleId);
						if(result != null)
						{
							out.write("<br/><br/><h2 class='title'>" + encoder.encodeForHTML(result[0]) + " Cheat</h2>" +
									"<p>" +
									result[1] +
									"</p>");
						}
					}
				}
			}
			else
			{
				log.error("CSRF Attack Detected: Made Against" + ses.getAttribute("userName"));
			}
		}
		else
		{
			out.write("<img src='" + encoder.encodeForHTMLAttribute(ExposedServer.getSecureUrl()) + "css/images/loggedOutSheep.jpg'/>");
		}
		if(result == null)
		{
			out.write("An error occured...");
		}
		log.debug("*** END servlets.Admin.GetCheat ***");
	}
}