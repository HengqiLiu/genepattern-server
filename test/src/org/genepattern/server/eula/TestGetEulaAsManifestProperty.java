package org.genepattern.server.eula;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.genepattern.junitutil.FileUtil;
import org.genepattern.server.config.ServerConfiguration.Context;
import org.genepattern.server.eula.EulaInfo.EulaInitException;
import org.genepattern.webservice.TaskInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * jUnit tests for the GetEulaAsManifestProperty class.
 * 
 * @author pcarr
 */
public class TestGetEulaAsManifestProperty {
    @Before
    public void setUp() {
        File thisDir=FileUtil.getSourceDir(this.getClass());
        EulaInfo.setLibdirStrategy( new LibdirStub(thisDir) );
    }
    
    @After
    public void tearDown() {
        EulaInfo.setLibdirStrategy(null);
    }
    
    /**
     * Test case: make sure we can get the list of EulaInfo from a single module,
     * which requires an EULA.
     */
    @Test
    public void testGetEulaFromModule() {
        final String filename="testLicenseAgreement_v3.zip";
        TaskInfo taskInfo = TestEulaManager.initTaskInfoFromZip(filename);
        Assert.assertNotNull("taskInfo==null", taskInfo);
        File licenseFile=FileUtil.getSourceFile(TestGetEulaAsManifestProperty.class, "gp_server_license.txt");

        GetEulaAsManifestProperty stub = new GetEulaAsManifestProperty();
        try {
            EulaInfo eulaIn=GetEulaFromTaskStub.initEulaInfo(taskInfo, licenseFile);
            stub.setEula(eulaIn, taskInfo);
        }
        catch (EulaInitException e) {
            Assert.fail(""+e.getLocalizedMessage());
        }

        final String userId="gp_user";
        Context taskContext=Context.getContextForUser(userId);
        taskContext.setTaskInfo(taskInfo);
        
        final List<EulaInfo> eulas=stub.getEulasFromTask(taskInfo);
        Assert.assertNotNull("eulas==null", eulas);
        Assert.assertEquals("Expecting one EulaInfo", 1, eulas.size());
        final String expectedContent=EulaInfo.fileToString(licenseFile);
        TestEulaManager.assertEulaInfo(eulas, 0, "testLicenseAgreement", "urn:lsid:9090.gpdev.gpint01:genepatternmodules:812:3", "3", expectedContent);
    }

    /**
     * Allow for the possibility of multiple EULA declared in the same manifest file.
     */
    @Test
    public void testGetTwoEulaFromOneModule() {
        TaskInfo taskInfo=new TaskInfo();
        taskInfo.setName("testLicenseAgreement");
        taskInfo.giveTaskInfoAttributes().put("LSID", "urn:lsid:9090.gpdev.gpint01:genepatternmodules:812:3");
        
        File gpLicenseFile=FileUtil.getSourceFile(TestGetEulaAsManifestProperty.class, "gp_server_license.txt");
        File exampleLicenseFile=FileUtil.getSourceFile(TestGetEulaAsManifestProperty.class, "example_license.txt");

        GetEulaAsManifestProperty stub = new GetEulaAsManifestProperty();
        try {
            EulaInfo eulaIn0=GetEulaFromTaskStub.initEulaInfo(taskInfo, gpLicenseFile);
            EulaInfo eulaIn1=GetEulaFromTaskStub.initEulaInfo(taskInfo, exampleLicenseFile);
            List<EulaInfo> eulasIn=new ArrayList<EulaInfo>();
            eulasIn.add(eulaIn0);
            eulasIn.add(eulaIn1);
            stub.setEulas(eulasIn, taskInfo);
        }
        catch (EulaInitException e) {
            Assert.fail(""+e.getLocalizedMessage());
        }

        final String userId="gp_user";
        Context taskContext=Context.getContextForUser(userId);
        taskContext.setTaskInfo(taskInfo);
        
        final List<EulaInfo> eulas=stub.getEulasFromTask(taskInfo);
        Assert.assertNotNull("eulas==null", eulas);
        Assert.assertEquals("Expecting two EulaInfo", 2, eulas.size());
        final String gpLicenseContent=EulaInfo.fileToString(gpLicenseFile);
        final String exampleLicenseContent=EulaInfo.fileToString(exampleLicenseFile);
        
        TestEulaManager.assertEulaInfo(eulas, 0, "testLicenseAgreement", "urn:lsid:9090.gpdev.gpint01:genepatternmodules:812:3", "3", gpLicenseContent);
        TestEulaManager.assertEulaInfo(eulas, 1, "testLicenseAgreement", "urn:lsid:9090.gpdev.gpint01:genepatternmodules:812:3", "3", exampleLicenseContent);
    }

}