/****************************************************************************
 * FILE: PropsDecryptor.java
 * DSCRPT: 
 ****************************************************************************/





package gcs.toolset.lattools.cfg;





import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.util.text.BasicTextEncryptor;



import lombok.extern.slf4j.Slf4j;





@Slf4j
public class PropsDecryptor
{
    private static final long serialVersionUID = 1306480900200475926L;





    public static final String decrypt(final XMLConfiguration cfg_, final String key_)
    {
        final String encryptedString = cfg_.getString(key_);
        if (StringUtils.isEmpty(encryptedString))
        {
            _logger.error("not found, key:{}", key_);
            return null;
        }

        final BasicTextEncryptor encryptor = new BasicTextEncryptor();
        encryptor.setPassword(Long.toString(serialVersionUID));
        return encryptor.decrypt(encryptedString);
    }

}

