package base.client.event.events;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Cancellable implements Event {
    boolean canceled;
    public void cancel(){
        this.setCanceled(true);
    }

    public boolean isCancelled() {
        return this.isCanceled();
    }

    public void setCancelled(boolean cancel) {
        this.setCanceled(cancel);
    }
}
