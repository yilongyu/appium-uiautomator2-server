package io.appium.uiautomator2.handler;

import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import java.util.List;

import io.appium.uiautomator2.common.exceptions.ElementNotFoundException;
import io.appium.uiautomator2.common.exceptions.UiAutomator2Exception;
import io.appium.uiautomator2.core.AxNodeInfoExtractor;
import io.appium.uiautomator2.handler.request.SafeRequestHandler;
import io.appium.uiautomator2.http.AppiumResponse;
import io.appium.uiautomator2.http.IHttpRequest;
import io.appium.uiautomator2.model.AndroidElement;
import io.appium.uiautomator2.model.AppiumUIA2Driver;
import io.appium.uiautomator2.model.Session;
import io.appium.uiautomator2.utils.Logger;

/**
 * This method return first visible element inside provided element
 */
public class FirstVisibleView extends SafeRequestHandler {

    public FirstVisibleView(String mappedUri) {
        super(mappedUri);
    }

    @Override
    protected AppiumResponse safeHandle(IHttpRequest request) throws UiObjectNotFoundException {
        Logger.info("Get first visible element inside provided element");
        String elementId = getElementId(request);
        Session session = AppiumUIA2Driver.getInstance().getSessionOrThrow();

        AndroidElement element = session.getElementsCache().get(elementId);
        Object firstObject = null;
        if (element.getUiObject() instanceof UiObject) {
            UiObject uiObject = (UiObject) element.getUiObject();
            Logger.debug("Container for first visible is a uiobject; looping through children");
            for (int i = 0; i < uiObject.getChildCount(); i++) {
                UiObject object = uiObject.getChild(new UiSelector().index(i));
                if (object.exists()) {
                    firstObject = object;
                    break;
                }
            }
        } else {
            UiObject2 uiObject = (UiObject2) element.getUiObject();
            Logger.debug("Container for first visible is a uiobject2; looping through children");
            List<UiObject2> childObjects = uiObject.getChildren();
            if (childObjects.isEmpty()) {
                throw new UiObjectNotFoundException("Could not get children for container object");
            }
            for (UiObject2 childObject : childObjects) {
                try {
                    if (AxNodeInfoExtractor.toNullableAxNodeInfo(childObject) != null) {
                        firstObject = childObject;
                        break;
                    }
                } catch (UiAutomator2Exception ignored) {
                }
            }
        }

        if (firstObject == null) {
            throw new ElementNotFoundException();
        }

        AndroidElement androidElement = session.getElementsCache().add(firstObject, true);
        return new AppiumResponse(getSessionId(request), androidElement.toModel());
    }
}
