package com.moonymango.snare.events;

import com.moonymango.snare.ui.widgets.BaseTouchWidget;

public interface IWidgetTouchedBeginEvent extends IEvent {

	SystemEventType EVENT_TYPE = SystemEventType.UI_WIDGET_TOUCHED_BEGIN;
	
	/**
	 * @return Widget which was touched/pressed.
	 */
	BaseTouchWidget getWidget();
	void setWidgetData(BaseTouchWidget widget);
	
}
