package com.liferay.demo;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.BaseFilter;
import com.liferay.portal.kernel.util.Validator;

/**
 * @author carlos.hernandez
 */

@Component(
		 immediate = true, 
		 property = {
		 "servlet-context-name=", 
		 "servlet-filter-name=FurlFilter",
		 "url-pattern="+CleanURLDLImpl.DOCUMENTS_DELIMITER+"/*",
		 }, 
		 service = Filter.class
)
public class DLCleanURLServletFilter extends BaseFilter {

		private static final Log _log = LogFactoryUtil.getLog(DLCleanURLServletFilter.class);
		@Override
		protected Log getLog() { return _log; }

	    /**
	     *
	     * Processes the requests and searches for the DLFileEntry according to the FURL.
	     * If no DLFileEntry is found, keep processing the FilterChain.
	     *
	     * @param request
	     * @param response
	     * @param filterChain
	     * @throws Exception
	     */
		@Override
		protected void processFilter(
				HttpServletRequest request, HttpServletResponse response,
				FilterChain filterChain)
			throws Exception {
			
			_log.error("filtering");
			String originalURI = request.getRequestURI();
			String newUri=originalURI.replaceAll(CleanURLDLImpl.DOCUMENTS_DELIMITER, "/webdav/guest/document_library");

			String data = request.getPathInfo();
			request.getRequestDispatcher(newUri).forward(request, response);
			if (Validator.isNull(data)) {
					if (_log.isWarnEnabled()) {
						_log.warn("Invalid url "+request.getRequestURI());
					}
					return;
			}

			processFilter( DLCleanURLServletFilter.class.getName(), request, response, filterChain);
		}

		
}

