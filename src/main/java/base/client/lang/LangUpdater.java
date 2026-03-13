package base.client.lang;

import com.google.gson.JsonObject;

public interface LangUpdater {
     JsonObject save();

     void load(JsonObject object);

}