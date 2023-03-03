/****************************************************************************
 * FILE: EntryPointModule.java
 * DSCRPT: 
 ****************************************************************************/





package gcs.toolset.lattools.cfg;





import javax.inject.Singleton;



import gcs.toolset.lattools.EntryPoint;



import dagger.Module;
import dagger.Provides;





@Module
public class EntryPointModule
{
    @Singleton
    @Provides
    public static EntryPoint buildEntryPoint(final AppProps props_)
    {
        return new EntryPoint(props_);
    }




  /*
    @Singleton
    @Provides
    public static DbService buildDbService(final DSLContext ctx_)
    {
        _logger.info("initializing db-service");
        return new DbService(ctx_);
    }





    @Singleton
    @Provides
    @SneakyThrows
    public static DataSource buildDbDs(final DbProps props_)
    {
        HikariConfig cfg = new HikariConfig();
        cfg.setUsername(props_.getUsername());
        cfg.setPassword(props_.getPassword());
        cfg.setJdbcUrl(props_.getConnectionStr());

        HikariDataSource pool = new HikariDataSource(cfg);
        return pool;
    }





    @Singleton
    @Provides
    @SneakyThrows
    public static DSLContext buildDsl(final DataSource conn_)
    {
        return DSL.using(conn_, SQLDialect.MYSQL);
    }
*/
}
