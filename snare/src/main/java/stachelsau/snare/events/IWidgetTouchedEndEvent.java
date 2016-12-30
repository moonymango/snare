package stachelsau.snare.events;

import stachelsau.snare.ui.widgets.BaseTouchWidget;

public interface IWidgetTouchedEndEvent extends IEvent {

	public static final SystemEventType EVENT_TYPE = SystemEventType.UI_WIDGET_TOUCHED_END;
	
	/**
	 * @return Widget which was touched/pressed.
	 */
	BaseTouchWidget getWidget();
	void setWidgetData(BaseTouchWidget widget);
}