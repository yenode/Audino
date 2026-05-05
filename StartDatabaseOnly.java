import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import java.io.File;
import java.util.Scanner;

public class StartDatabaseOnly {
    public static void main(String[] args) throws Exception {
        System.out.println("Starting Headless PostgreSQL Server...");
        String dataDir = System.getProperty("user.dir") + "/data/pg-data/";
        
        EmbeddedPostgres pg = EmbeddedPostgres.builder()
                .setPort(5432)
                .setDataDirectory(new File(dataDir))
                .setCleanDataDirectory(false)
                .start();
                
        System.out.println("=========================================");
        System.out.println("Database is running on localhost:5432!");
        System.out.println("You can now connect using DBeaver or pgAdmin.");
        System.out.println("Press ENTER in this window to stop the database and exit...");
        System.out.println("=========================================");
        
        new Scanner(System.in).nextLine();
        pg.close();
        System.out.println("Database shut down successfully.");
    }
}
