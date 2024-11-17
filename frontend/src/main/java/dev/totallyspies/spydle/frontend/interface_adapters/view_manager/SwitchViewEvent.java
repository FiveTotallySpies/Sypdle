package dev.totallyspies.spydle.frontend.interface_adapters.view_manager;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/*
 * Fired when we want to switch views
 */
@Getter
public class SwitchViewEvent extends ApplicationEvent {

    private final String viewName;

    public SwitchViewEvent(Object source, String viewName) {
        super(source);
        this.viewName = viewName;
    }

}