package com.liferay.demo;

import java.util.Date;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.liferay.document.library.kernel.util.DL;
import com.liferay.document.library.kernel.util.DLUtil;
import com.liferay.document.library.kernel.util.ImageProcessorUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.FileVersion;
import com.liferay.portal.kernel.theme.PortletDisplay;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portlet.documentlibrary.util.DLImpl;
import com.liferay.trash.web.internal.constants.TrashPortletKeys;

/**
 * @author carlos.hernandez
 *
 * Updates the getPreviewURL so that it returns the new FURL if it exists in the expando tables
 */

@Component(immediate = true, service = DL.class)
 public class CleanURLDLImpl extends DLImpl {
	public static final String DOCUMENTS_DELIMITER = "/myfiles";
	public static final String JOURNAL_ARTICLE_ID = "JournalArticleId";

	
	//TODO: check _log.info's
	DL odDL=null;

    @Reference
    private DLUtil _dlUtil;
	
    @Activate
    public void activate() {
		_log.info("\n\n\noverriding DLImpl service\n\n\n");
		odDL = DLUtil.getDL();
        _dlUtil.setDL(this);
    }

	@Override
	public String getPreviewURL( FileEntry fileEntry, FileVersion fileVersion, ThemeDisplay themeDisplay, String queryString) {
		return getPreviewURL(fileEntry, fileVersion, themeDisplay, queryString, true, true);
	}

	@Override
	public String getPreviewURL(
		FileEntry fileEntry, FileVersion fileVersion, ThemeDisplay themeDisplay,
		String queryString, boolean appendVersion, boolean absoluteURL) {

		//Start to build friendlyURL
		//StringBundler sb = new StringBundler(15);
		StringBundler sb = new StringBundler();
		try {
			sb.append(getWebDavURL(themeDisplay, fileEntry.getFolder(), fileEntry).replaceAll( "/webdav/guest/document_library", DOCUMENTS_DELIMITER));
		} catch (PortalException e2) {
			e2.printStackTrace();
			return odDL.getPreviewURL(fileEntry, fileVersion, themeDisplay, queryString, appendVersion, absoluteURL);
		}

        if (appendVersion) {
            sb.append("?version=");
            sb.append(fileVersion.getVersion());
        }
        if (ImageProcessorUtil.isImageSupported(fileVersion)) {
            if (appendVersion) sb.append("&t=");
            else sb.append("?t=");

            Date modifiedDate = fileVersion.getModifiedDate();
            sb.append(modifiedDate.getTime());
        }

        sb.append(queryString);

        if (themeDisplay != null) {
            PortletDisplay portletDisplay = themeDisplay.getPortletDisplay();

            if (portletDisplay != null) {
                String portletId = portletDisplay.getId();
                if (portletId.equals(TrashPortletKeys.TRASH)) sb.append("&status=").append(WorkflowConstants.STATUS_IN_TRASH);
            }
        }
        String previewURL = sb.toString();
        if ((themeDisplay != null) && themeDisplay.isAddSessionIdToURL()) 
            return PortalUtil.getURLWithSessionId( previewURL, themeDisplay.getSessionId());
        else return previewURL;
	}

	private static final Log _log = LogFactoryUtil.getLog(CleanURLDLImpl.class);
	 
}
