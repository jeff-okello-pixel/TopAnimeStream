package urf.animestream.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class Package {
    private String name;
    private int version;
    private String description;
    private String manifestUrl;
    private String apkUrl;
    private String changes;

    public Package() {
    }
    public Package(JSONObject jsonPackage){
        try {
            this.setName(!jsonPackage.isNull("name") ? jsonPackage.getString("name") : null);
            this.setVersion(!jsonPackage.isNull("version") ? jsonPackage.getInt("version") : 0);
            this.setDescription(!jsonPackage.isNull("description") ? jsonPackage.getString("description") : null);
            this.setManifestUrl(!jsonPackage.isNull("manifestUrl") ? jsonPackage.getString("manifestUrl") : null);
            this.setApkUrl(!jsonPackage.isNull("apkUrl") ? jsonPackage.getString("apkUrl") : null);
            JSONObject jsonChanges = !jsonPackage.isNull("changes") ? jsonPackage.getJSONObject("changes") : null;
            if(jsonChanges != null)
            {
                String language = Locale.getDefault().getLanguage();
                String changes = !jsonChanges.isNull(language) ? jsonChanges.getString(language) : null;
                if(changes != null)
                {
                    this.changes = changes;
                }
                else
                {
                    this.changes = jsonChanges.getString("en");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    public Package(String name, int version, String description, String manifestUrl, String apkUrl, String changes) {
        this.name = name;
        this.version = version;
        this.description = description;
        this.manifestUrl = manifestUrl;
        this.apkUrl = apkUrl;
        this.changes = changes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getManifestUrl() {
        return manifestUrl;
    }

    public void setManifestUrl(String manifestUrl) {
        this.manifestUrl = manifestUrl;
    }

    public String getApkUrl() {
        return apkUrl;
    }

    public void setApkUrl(String apkUrl) {
        this.apkUrl = apkUrl;
    }

    public String getChanges() {
        return changes;
    }

    public void setChanges(String changes) {
        this.changes = changes;
    }
}
