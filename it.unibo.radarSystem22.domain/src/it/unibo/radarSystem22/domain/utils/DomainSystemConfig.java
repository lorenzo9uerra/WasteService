package it.unibo.radarSystem22.domain.utils;


public class DomainSystemConfig {
	// simulation false -> controllo simulateX specifici
	public static boolean simulation     = true;
	public static boolean simulateLed    = false;
	public static boolean simulateSonar  = false;
	public static boolean ledAvailable   = true;
	public static boolean sonarAvailable = true;
	public static boolean radarAvailable = false;

	public static String deviceScriptFolder = "../device-helpers/";

 	public static boolean ledGui         = false;
	public static boolean radarRemote    = false;
	public static boolean sudoRequired   = false;

	public static int sonarDelay          =  100;     
	public static int sonarDistanceMax    =  150;     
	public static int sonarMockDelta = -1;
	public static int sonarMockStartDist = 100;
	public static boolean sonarVerbose = false;
	public static boolean execVerbose = false;
    
	public static boolean tracing         = false;	
	public static boolean testing         = false;			
	

	public static void setTheConfiguration(  ) {
		setTheConfiguration("../DomainSystemConfig.json");
	}

	public static void setTheConfiguration( String resourceName ) {
		StaticConfig.setTheConfiguration(DomainSystemConfig.class, resourceName);
	}
}
