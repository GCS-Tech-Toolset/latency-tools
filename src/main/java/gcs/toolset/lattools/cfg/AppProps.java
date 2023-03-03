/****************************************************************************
 * FILE: CtrailProps.java
 * DSCRPT: 
 ****************************************************************************/





package gcs.toolset.lattools.cfg;





import java.util.Map;



import org.apache.commons.configuration2.XMLConfiguration;



import com.gcs.config.IProps;
import com.gcs.runtime.VersionInfo;



import lombok.extern.slf4j.Slf4j;





@Slf4j
public class AppProps implements IProps
{
    private static final long serialVersionUID = 1306480900200475926L;





    @Override
    public void loadFromXml(final XMLConfiguration cfg_)
    {
        // read config here
    }





    @Override
    public Map<String, String> toMap()
    {
        return null;
    }





    public String getVersion()
    {
        return VersionInfo.calcVersion(AppProps.class);
    }

}
