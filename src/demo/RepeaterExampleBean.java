/*
 The Broad Institute
 SOFTWARE COPYRIGHT NOTICE AGREEMENT
 This software and its documentation are copyright (2003-2011) by the
 Broad Institute/Massachusetts Institute of Technology. All rights are
 reserved.
 
 This software is supplied without any warranty or guaranteed support
 whatsoever. Neither the Broad Institute nor MIT can be responsible for its
 use, misuse, or functionality.
 */

package demo;

public class RepeaterExampleBean {
    private String text;

    public RepeaterExampleBean(){}
    
    public String getText() {
        System.out.println("GetText");
        return text;
    }

    public void setText(String text) {
        System.out.println("SetText");
        this.text = text;
    }
    
    public String submit() {
        return null;
    }
}
