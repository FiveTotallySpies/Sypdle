package dev.totallyspies.spydle.frontend.interface_adaptors.all_room_adaptors;

import dev.totallyspies.spydle.frontend.interface_adaptors.game_view_adaptors.SwitchViewSEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class AllRoomController {
    @Autowired
    private ApplicationEventPublisher publisher;

    /*
    Method called when View All Rooms Button is Pressed
     */
    public void changeView(String view) {
        publisher.publishEvent(new SwitchViewSEvent(this, view));
    }
}
