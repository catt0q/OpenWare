package base.client.event.events.impl.input;

import base.client.event.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EventMoveInput implements Event
{
    public boolean forward=false;
    public boolean backward=false;
    public boolean left=false;
    public boolean right=false;
    public boolean jump=false;
    public boolean sneak=false;
    public boolean sprint=false;


public void setfor(float val){
    if(val>0){
        forward=true; backward=false;
    }else if(val<0){
        forward=false; backward=true;
    }else {
        forward=false; backward=false;
    }
  }
    public void setstrafe(float val){
        if(val>0){
            right=true; left=false;
        }else if(val<0){
            right=false; left=true;
        }else {
            right=false; left=false;
        }
    }



    public float getstrafe() {
        float str=0;
        if(this.left) {
            str--;
        }
        if(this.right) {
            str++;
        }
        return str;
    }

    public float getfor() {
        float forv=0;
        if(this.backward) {
            forv--;
        }
        if(this.forward) {
            forv++;
        }
        return forv;
    }




}
