/****************************************************************************
 * FILE: TxFileParserEntryPoint.java
 * DSCRPT: 
 ****************************************************************************/





package gcs.toolset.lattools;





import javax.inject.Inject;



import gcs.toolset.lattools.cfg.AppMainComponent;
import gcs.toolset.lattools.cfg.DaggerAppMainComponent;



import gcs.toolset.lattools.cfg.AppProps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;





@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class EntryPoint
{

    //
    // dagger
    //
    final static AppMainComponent _comp;

    static
    {
        _comp = DaggerAppMainComponent.create();
    }




    //
    // my objects of interest
    //
    private final AppProps _props;





    //
    // actual entry point
    //
    public static void main(String[] args_)
    {
        EntryPoint ep = _comp.entryPoint();
    }

}
