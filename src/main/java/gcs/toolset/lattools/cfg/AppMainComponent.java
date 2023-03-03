/****************************************************************************
 * FILE: MktDataPublisherComponent.java
 * DSCRPT: 
 ****************************************************************************/





package gcs.toolset.lattools.cfg;





import javax.inject.Singleton;



import com.gcs.metrics.AppMetrics;

import gcs.toolset.lattools.EntryPoint;



import dagger.Component;





@Singleton
@Component(modules =
{
        AppPropsModule.class,
        EntryPointModule.class
})
public interface AppMainComponent
{
    public EntryPoint entryPoint();





    public AppProps appProps();





    public AppMetrics appMetrics();


}
