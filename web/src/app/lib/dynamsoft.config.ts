import { CoreModule } from "dynamsoft-core";
import { LicenseManager } from "dynamsoft-license";
import "dynamsoft-barcode-reader";
import { environment } from "environments/environment";

CoreModule.engineResourcePaths.rootDirectory = "https://cdn.jsdelivr.net/npm/";

LicenseManager.initLicense(environment.dynamsoft.license);

CoreModule.loadWasm(['dbr']);