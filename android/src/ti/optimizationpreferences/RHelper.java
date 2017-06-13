package ti.optimizationpreferences;

import org.appcelerator.titanium.util.TiRHelper;
import org.appcelerator.titanium.util.TiRHelper.ResourceNotFoundException;

public class RHelper {

    public static int getString(String str) {
        try {
            return TiRHelper.getApplicationResource("string." + str);
        } catch (ResourceNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
