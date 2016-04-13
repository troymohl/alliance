package com.connexta.alliance.nsili.mockserver.client;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.helpers.IOUtils;
import com.connexta.alliance.nsili.common.GIAS.AccessCriteria;
import com.connexta.alliance.nsili.common.GIAS.AlterationSpec;
import com.connexta.alliance.nsili.common.GIAS.AttributeInformation;
import com.connexta.alliance.nsili.common.GIAS.CatalogMgr;
import com.connexta.alliance.nsili.common.GIAS.CatalogMgrHelper;
import com.connexta.alliance.nsili.common.GIAS.CreationMgr;
import com.connexta.alliance.nsili.common.GIAS.CreationMgrHelper;
import com.connexta.alliance.nsili.common.GIAS.DataModelMgr;
import com.connexta.alliance.nsili.common.GIAS.DataModelMgrHelper;
import com.connexta.alliance.nsili.common.GIAS.DeliveryDetails;
import com.connexta.alliance.nsili.common.GIAS.DeliveryManifest;
import com.connexta.alliance.nsili.common.GIAS.DeliveryManifestHolder;
import com.connexta.alliance.nsili.common.GIAS.Destination;
import com.connexta.alliance.nsili.common.GIAS.GeoRegionType;
import com.connexta.alliance.nsili.common.GIAS.GetParametersRequest;
import com.connexta.alliance.nsili.common.GIAS.GetRelatedFilesRequest;
import com.connexta.alliance.nsili.common.GIAS.HitCountRequest;
import com.connexta.alliance.nsili.common.GIAS.Library;
import com.connexta.alliance.nsili.common.GIAS.LibraryDescription;
import com.connexta.alliance.nsili.common.GIAS.LibraryHelper;
import com.connexta.alliance.nsili.common.GIAS.LibraryManager;
import com.connexta.alliance.nsili.common.GIAS.MediaType;
import com.connexta.alliance.nsili.common.GIAS.OrderContents;
import com.connexta.alliance.nsili.common.GIAS.OrderMgr;
import com.connexta.alliance.nsili.common.GIAS.OrderMgrHelper;
import com.connexta.alliance.nsili.common.GIAS.OrderRequest;
import com.connexta.alliance.nsili.common.GIAS.PackageElement;
import com.connexta.alliance.nsili.common.GIAS.PackagingSpec;
import com.connexta.alliance.nsili.common.GIAS.Polarity;
import com.connexta.alliance.nsili.common.GIAS.ProductDetails;
import com.connexta.alliance.nsili.common.GIAS.ProductMgr;
import com.connexta.alliance.nsili.common.GIAS.ProductMgrHelper;
import com.connexta.alliance.nsili.common.GIAS.Query;
import com.connexta.alliance.nsili.common.GIAS.SortAttribute;
import com.connexta.alliance.nsili.common.GIAS.SubmitQueryRequest;
import com.connexta.alliance.nsili.common.GIAS.TailoringSpec;
import com.connexta.alliance.nsili.common.GIAS.ValidationResults;
import com.connexta.alliance.nsili.common.NsiliConstants;
import com.connexta.alliance.nsili.common.NsiliManagerType;
import com.connexta.alliance.nsili.common.UCO.AbsTime;
import com.connexta.alliance.nsili.common.UCO.AbsTimeHelper;
import com.connexta.alliance.nsili.common.UCO.Coordinate2d;
import com.connexta.alliance.nsili.common.UCO.DAG;
import com.connexta.alliance.nsili.common.UCO.DAGHolder;
import com.connexta.alliance.nsili.common.UCO.DAGListHolder;
import com.connexta.alliance.nsili.common.UCO.Date;
import com.connexta.alliance.nsili.common.UCO.FileLocation;
import com.connexta.alliance.nsili.common.UCO.NameListHolder;
import com.connexta.alliance.nsili.common.UCO.NameName;
import com.connexta.alliance.nsili.common.UCO.NameValue;
import com.connexta.alliance.nsili.common.UCO.Node;
import com.connexta.alliance.nsili.common.UCO.NodeType;
import com.connexta.alliance.nsili.common.UCO.Rectangle;
import com.connexta.alliance.nsili.common.UCO.RectangleHelper;
import com.connexta.alliance.nsili.common.UCO.Time;
import com.connexta.alliance.nsili.common.UID.Product;
import com.connexta.alliance.nsili.common.UID._ProductStub;
import com.connexta.alliance.nsili.transformer.DAGConverter;

import org.omg.CORBA.Any;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TCKind;
import org.omg.PortableServer.POA;

public class NsiliClient {

    private static final String DEFAULT_USER_INFO = "Alliance";

    private static Library library;

    private static CatalogMgr catalogMgr;

    private static OrderMgr orderMgr;

    private static ProductMgr productMgr;

    private static DataModelMgr dataModelMgr;

    private static CreationMgr creationMgr;

    private static final String WGS84 = "WGS84";

    private static final String GEOGRAPHIC_DATUM = "GeographicDatum";

    private static final AccessCriteria accessCriteria = new AccessCriteria("test", "test", "");

    private String ddfOrgName = DEFAULT_USER_INFO;

    private Integer maxHitCount = 200;

    private ORB orb;

    public NsiliClient(ORB orb) {
        this.orb = orb;
    }

    public void initLibrary(String iorFilePath) throws Exception {
        iorFilePath = iorFilePath.trim();
        org.omg.CORBA.Object obj = orb.string_to_object(iorFilePath);
        if (obj == null) {
            System.err.println("Cannot read " + iorFilePath);
        }

        library = LibraryHelper.narrow(obj);
        System.out.println("Library Initialized");
    }

    public String[] getManagerTypes() throws Exception {
        LibraryDescription libraryDescription = library.get_library_description();
        System.out.println("NAME: " + libraryDescription.library_name + ", DESCRIPTION: "
                + libraryDescription.library_description + ", VERSION: "
                + libraryDescription.library_version_number);
        String[] types = library.get_manager_types();
        System.out.println("Got Manager Types from " + libraryDescription.library_name + " : ");
        for (String type : types) {
            System.out.println("\t" + type);
        }
        System.out.println();
        return types;
    }

    public void initManagers(String[] managers) throws Exception {
        for (String managerType : managers) {
            System.out.println("Attempting to initialize manager type: " + managerType);

            if (managerType.equals(NsiliManagerType.CATALOG_MGR.getSpecName())) {
                // Get Mandatory Managers
                System.out.println("Getting CatalogMgr from source...");
                LibraryManager libraryManager = library.get_manager("CatalogMgr", accessCriteria);
                catalogMgr = CatalogMgrHelper.narrow(libraryManager);
                System.out.println("Source returned : " + catalogMgr.getClass() + "\n");
            } else if (managerType.equals(NsiliManagerType.ORDER_MGR.getSpecName())) {

                System.out.println("Getting OrderMgr from source...");
                LibraryManager libraryManager = library.get_manager("OrderMgr", accessCriteria);
                orderMgr = OrderMgrHelper.narrow(libraryManager);
                System.out.println("Source returned : " + orderMgr.getClass() + "\n");
            } else if (managerType.equals(NsiliManagerType.PRODUCT_MGR.getSpecName())) {
                System.out.println("Getting ProductMgr from source...");
                LibraryManager libraryManager = library.get_manager("ProductMgr", accessCriteria);
                productMgr = ProductMgrHelper.narrow(libraryManager);
                System.out.println("Source returned : " + productMgr.getClass() + "\n");
            } else if (managerType.equals(NsiliManagerType.DATA_MODEL_MGR.getSpecName())) {
                System.out.println("Getting DataModelMgr from source...");
                LibraryManager libraryManager = library.get_manager("DataModelMgr", accessCriteria);
                dataModelMgr = DataModelMgrHelper.narrow(libraryManager);
                System.out.println("Source returned : " + dataModelMgr.getClass() + "\n");
            } else if (managerType.equals(NsiliManagerType.CREATION_MGR.getSpecName())) {
                System.out.println("Getting CreationMgr from source...");
                try {
                    LibraryManager libraryManager = library.get_manager("CreationMgr",
                            accessCriteria);
                    creationMgr = CreationMgrHelper.narrow(libraryManager);
                } catch (Exception e) {
                    System.err.println("Unable to initialize CreationMgr: "+e.getLocalizedMessage());
                }
            }
        }
    }

    public int getHitCount(Query query) throws Exception {
        if (catalogMgr != null) {
            System.out.println("Getting Hit Count From Query...");
            HitCountRequest hitCountRequest = catalogMgr.hit_count(query, new NameValue[0]);
            IntHolder intHolder = new IntHolder();
            hitCountRequest.complete(intHolder);
            System.out.println("Server responded with " + intHolder.value + " hit(s).\n");
            return intHolder.value;
        } else {
            System.out.println("catalogMgr was not initialized, unable to find hit count");
            return -1;
        }
    }

    public DAG[] submit_query(Query query) throws Exception {
        if (catalogMgr != null) {
            System.out.println("Submitting Query To Server...");
            DAGListHolder dagListHolder = new DAGListHolder();
            AttributeInformation[] attributeInformationArray =
                    dataModelMgr.get_attributes(NsiliConstants.NSIL_ALL_VIEW, new NameValue[0]);
            String[] resultAttributes = new String[attributeInformationArray.length];
            for (int c = 0; c < attributeInformationArray.length; c++) {
                AttributeInformation attributeInformation = attributeInformationArray[c];
                resultAttributes[c] = attributeInformation.attribute_name;
            }

            SortAttribute sortAttr = new SortAttribute("NSIL_CARD.identifier", Polarity.ASCENDING);

            SubmitQueryRequest submitQueryRequest = catalogMgr.submit_query(query,
                    resultAttributes,
                    new SortAttribute[]{sortAttr},
                    getDefaultPropertyList());

            System.out.println("Query has been submitted");
            submitQueryRequest.set_user_info(ddfOrgName);
            submitQueryRequest.set_number_of_hits(maxHitCount);
            submitQueryRequest.complete_DAG_results(dagListHolder);
            System.out.println(
                    "Server Responded with " + dagListHolder.value.length + " result(s).\n");
            return dagListHolder.value;
        } else {
            System.out.println("catalogMgr is not iniitalized, unable to submit queries");
            return null;
        }
    }

    public void processAndPrintResults(DAG[] results) {
        System.out.println("Printing DAG Attribute Results...");
        for (int i = 0; i < results.length; i++) {
            DAGConverter.printDAG(results[i]);
            try {
                retrieveProductFromDAG(results[i]);
            } catch (MalformedURLException e) {
                System.out.println("Invalid URL used for product retrieval.");
            }

        }
    }

    public void printDAGAttributes(DAG dag) {
        for (int i = 0; i < dag.nodes.length; i++) {
            Node node = dag.nodes[i];
            if (node.node_type.equals(NodeType.ATTRIBUTE_NODE)) {

                String result = null;

                if (node.value.type().kind() == TCKind.tk_wstring) {
                    result = node.value.extract_wstring();
                } else if (node.value.type().kind() == TCKind.tk_string) {
                    result = node.value.extract_string();
                } else if (node.value.type().kind() == TCKind.tk_long) {
                    result = String.valueOf(node.value.extract_ulong());
                } else if (node.value.type().kind() == TCKind.tk_ulong) {
                    result = String.valueOf(node.value.extract_long());
                } else if (node.value.type()
                        .toString()
                        .contains("AbsTime")) {
                    AbsTime absTime = AbsTimeHelper.extract(node.value);
                    String absDate =
                            absTime.aDate.month + "/" + absTime.aDate.day + "/" + absTime.aDate.year
                                    + ":";
                    String absHour = absTime.aTime.hour + ":" + absTime.aTime.minute + ":"
                            + absTime.aTime.second;
                    result = absDate + absHour;
                } else if (node.value.type().kind() == TCKind.tk_boolean) {
                    result = "" + node.value.extract_boolean();
                } else if (node.value.type()
                        .toString()
                        .contains("Rectangle")) {
                    Rectangle rectangle = RectangleHelper.extract(node.value);
                    result = "" + rectangle.lower_right.x + "," + rectangle.lower_right.y + " "
                            + rectangle.upper_left.x + "," + rectangle.upper_left.y;
                } else if (node.value.type().kind() == TCKind.tk_short) {
                    result = String.valueOf(node.value.extract_short());
                } else if (node.value.type().kind() == TCKind.tk_double) {
                    result = String.valueOf(node.value.extract_double());
                }
                System.out.printf("%25s : %s%n", node.attribute_name, result);
            }
        }
        System.out.println();
    }

    public void retrieveProductFromDAG(DAG dag) throws MalformedURLException {
        System.out.println("Downloading products...");
        for (int i = 0; i < dag.nodes.length; i++) {
            Node node = dag.nodes[i];
            if (node.attribute_name.equals("productUrl")) {

                String url = node.value.extract_string();
                URL fileDownload = new URL(url);
                String productPath = "product.jpg";
                System.out.println("Downloading product : " + url);
                try (FileOutputStream outputStream = new FileOutputStream(new File(productPath));
                        BufferedInputStream inputStream = new BufferedInputStream(fileDownload.openStream());
                ) {

                    byte[] data = new byte[1024];
                    int count;
                    while ((count = inputStream.read(data, 0, 1024)) != -1) {
                        outputStream.write(data, 0, count);
                    }

                    System.out.println("Successfully downloaded product from " + url + ".\n");
                    Files.deleteIfExists(Paths.get(productPath));

                } catch (IOException e) {
                    System.out.println("Unable to download product from " + url + ".\n");
                    e.printStackTrace();
                }
            }
        }
    }

    public void validate_order(ORB orb) throws Exception {
        if (orderMgr != null) {
            System.out.println("Sending a Validate Order Request to Server...");

            NameValue[] properties = {new NameValue("", orb.create_any())};
            OrderContents order = createOrder(orb);
            ValidationResults validationResults = orderMgr.validate_order(order, properties);

            System.out.println("Validation Results: ");
            System.out.println("\tValid : " + validationResults.valid + "\n\tWarning : "
                    + validationResults.warning + "\n\tDetails : " + validationResults.details
                    + "\n");
        } else {
            System.out.println("orderMgr is not initialized, unable to validate order request");
        }
    }

    public PackageElement[] order(ORB orb) throws Exception {
        if (orderMgr != null) {
            System.out.println("Sending order request...");
            NameValue[] properties = {new NameValue("", orb.create_any())};
            OrderContents order = createOrder(orb);

            OrderRequest validationResults = orderMgr.order(order, properties);
            System.out.println("Completing OrderRequest...");

            DeliveryManifestHolder deliveryManifestHolder = new DeliveryManifestHolder();
            validationResults.complete(deliveryManifestHolder);
            DeliveryManifest deliveryManifest = deliveryManifestHolder.value;

            System.out.println("Completed Order :");
            System.out.println(deliveryManifest.package_name);

            PackageElement[] elements = deliveryManifest.elements;
            for (int i = 0; i < elements.length; i++) {

                String[] files = elements[i].files;

                for (int c = 0; c < files.length; c++) {
                    System.out.println("\t" + files[c]);
                }

            }
            System.out.println();
            return elements;
        } else {
            System.out.println("orderMgr is not initialized, unable to submit order");
            return null;
        }
    }

    public void get_parameters(ORB orb, Product product) throws Exception {
        if (productMgr != null) {
            System.out.println("Sending Get Parameters Request to Server...");

            String[] desired_parameters = {"param1", "param2"};
            NameValue[] properties = {new NameValue("", orb.create_any())};

            GetParametersRequest parametersRequest = productMgr.get_parameters(product,
                    desired_parameters,
                    properties);
            System.out.println("Completing GetParameters Request ...");

            DAGHolder dagHolder = new DAGHolder();
            parametersRequest.complete(dagHolder);

            DAG dag = dagHolder.value;
            System.out.println("Resulting Parameters From Server :");
            printDAGAttributes(dag);
            System.out.println();
        } else {
            System.out.println("productMgr is not initialized, unable to get parameters");
        }
    }

    public void get_related_file_types(Product product) throws Exception {
        if (productMgr != null) {
            System.out.println("Sending Get Related File Types Request...");
            String[] related_file_types = productMgr.get_related_file_types(product);
            System.out.println("Related File Types : ");
            for (int i = 0; i < related_file_types.length; i++) {
                System.out.println(related_file_types[i]);
            }
            System.out.println();
        } else {
            System.out.println("productMgr is not initialized, unable to get related file types");
        }
    }

    public void get_related_files(ORB orb, Product product) throws Exception {
        if (productMgr != null) {
            System.out.println("Sending Get Related Files Request...");

            FileLocation fileLocation = new FileLocation("", "", "", "", "");
            NameValue[] properties = {new NameValue("", orb.create_any())};
            Product[] products = {product};

            GetRelatedFilesRequest relatedFilesRequest = productMgr.get_related_files(products,
                    fileLocation,
                    "",
                    properties);
            System.out.println("Completing GetRelatedFilesRequest...");

            NameListHolder locations = new NameListHolder();

            relatedFilesRequest.complete(locations);

            System.out.println("Location List : ");
            String[] locationList = locations.value;
            for (int i = 0; i < locationList.length; i++) {
                System.out.println(locationList[i]);
            }
            System.out.println();
        } else {
            System.out.println("productMgr is not initialized, unable to get related files");
        }
    }

    public OrderContents createOrder(ORB orb) throws Exception {
        NameName nameName[] = {new NameName("", "")};

        TailoringSpec tailoringSpec = new TailoringSpec(nameName);
        PackagingSpec pSpec = new PackagingSpec("", "");
        AbsTime needByDate = new AbsTime(new Date((short) 2, (short) 10, (short) 16),
                new Time((short) 10, (short) 0, (short) 0));

        MediaType[] mTypes = {new MediaType("", (short) 1)};
        String[] benums = {""};
        AlterationSpec aSpec = new AlterationSpec("",
                orb.create_any(),
                new Rectangle(new Coordinate2d(1.1, 1.1), new Coordinate2d(2.2, 2.2)),
                GeoRegionType.NULL_REGION);
        Product product = new _ProductStub();

        Destination destination = new Destination();
        destination.e_dest("");

        ProductDetails[] productDetails = {new ProductDetails(mTypes, benums, aSpec, product, "")};
        DeliveryDetails[] deliveryDetails = {new DeliveryDetails(destination, "", "")};

        OrderContents order = new OrderContents("DDF",
                tailoringSpec,
                pSpec,
                needByDate,
                "Give me an order!",
                (short) 1,
                productDetails,
                deliveryDetails);

        return order;
    }

    public String getIorTextFile(String iorURL) throws Exception {
        System.out.println("Downloading IOR File From Server...");
        String myString = "";

        try {
            //Disable certificate checking as this is only a test client
            doTrustAllCertificates();
            URL fileDownload = new URL(iorURL);
            BufferedInputStream inputStream = new BufferedInputStream(fileDownload.openStream());
            myString = IOUtils.toString(inputStream, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (StringUtils.isNotBlank(myString)) {
            System.out.println("Successfully Downloaded IOR File From Server.\n");
            return myString;
        }

        throw new Exception("Error recieving IOR File");
    }

    // Trust All Certifications
    private void doTrustAllCertificates() throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s)
                    throws CertificateException {
                return;
            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s)
                    throws CertificateException {
                return;
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        }};

        // Set HttpsURLConnection settings
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        HostnameVerifier hostnameVerifier =
                (s, sslSession) -> s.equalsIgnoreCase(sslSession.getPeerHost());
        HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
    }

    /**
     * Returns the Default Property List defined in the STANAG 4559 specification.
     *
     * @return - default WGS84 Geographic Datum.
     */
    private NameValue[] getDefaultPropertyList() {
        Any defaultAnyProperty = orb.create_any();
        defaultAnyProperty.insert_string(WGS84);
        NameValue[] result = {new NameValue(GEOGRAPHIC_DATUM, defaultAnyProperty)};
        return result;
    }
}