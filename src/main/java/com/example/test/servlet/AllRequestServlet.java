package com.example.test.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.CharArrayWriter;

import org.apache.log4j.Logger;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Collections;

import javax.servlet.ServletException;
import javax.servlet.Filter;
import javax.servlet.ServletRequest;
import javax.servlet.ServletContext;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import java.net.URI;
import java.util.List;
import java.util.Date;
import java.util.Arrays;
import java.util.Map;
import java.util.ArrayList;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.security.Permission;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.pages.Page;
// import com.atlassian.confluence.pages.actions.PageAware;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.confluence.core.ContentPermissionManager;
import com.atlassian.plugin.servlet.PluginHttpRequestWrapper;
import com.atlassian.confluence.security.ContentPermission;
import com.atlassian.confluence.security.ContentPermissionSet;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.sal.api.web.context.HttpContext;


import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import javax.inject.Inject;


public class AllRequestServlet implements Filter{
    private String[] idPatterns = {
        "/quickreload/latest/",
        "/likes/1.0/content/",
        "/api/content/",
        "/rest/likes/1.0/content/"
        
    };

    private String[] staticPatterns = {
        "/plugins/pagetree/naturalchildren.action",
    };

    private String cattedPattern = "(" + String.join("|", idPatterns) + ")(?<pageId>\\d+)(/.*)?";
    private Pattern staticPattern = Pattern.compile("/plugins/pagetree/naturalchildren.action");
    
    private Pattern pageIdPattern = Pattern.compile(cattedPattern);
    // Pattern.compile("viewpage.action?pageId=(?<pageId>\d+)#"
    private Pattern spaceAndPagePattern = Pattern.compile("/display/(?<spaceKey>[a-zA-Z0-9]+)/(?<page>[^/]+)");
    // "/confluence/rest/api/content/1507332"
    
    @ComponentImport
    private final UserManager userManager;
    @ComponentImport
    private final UserAccessor userAccessor;
    @ComponentImport
    private final LoginUriProvider loginUriProvider;
    @ComponentImport
    private final PermissionManager permissionManager;

    @ComponentImport
    private final ContentPermissionManager contentPermissionManager;

    // @ComponentImport
    // private final ContentService contentService;

    @ComponentImport
    private final PageManager pageManager;

    private boolean fail = false;

    Logger logger = Logger.getLogger(AllRequestServlet.class);
    private FilterConfig config;

    @Inject
    public AllRequestServlet(
        UserManager userManager,
        LoginUriProvider loginUriProvider,
        UserAccessor userAccessor,
        PermissionManager permissionManager,
        PageManager pageManager,
        ContentPermissionManager contentPermissionManager
        //ContentService contentService
        ){
            this.userManager = userManager;
            this.loginUriProvider = loginUriProvider;
            this.userAccessor = userAccessor;
            this.permissionManager = permissionManager;
            this.pageManager = pageManager;
            this.contentPermissionManager = contentPermissionManager;
            //this.contentService = contentService;
    }

    @Override
    public void init(FilterConfig config){
        this.config = config;

    }

    private Page getPage(String uri, String pageIdQueryParam){
        System.out.println("====================getting page==========================");
        String pathInfo = uri.split("/confluence")[1];
        System.out.println("pathInfo  :  " + pathInfo);
        System.out.println("pageIdQueryParam  :  " + pageIdQueryParam);
        if (pathInfo == null ) return null;
        Matcher pageId =  this.pageIdPattern.matcher(pathInfo);
        Matcher spaceAndPage =  this.spaceAndPagePattern.matcher(pathInfo);
        Matcher staticPattern =  this.staticPattern.matcher(pathInfo);


        if (pageId.matches() || pageIdQueryParam != null) {
            System.out.println("pageid Matched ++++++++++++++++++  :  ");
            String confluencePageId = pageId.matches() 
                ? pageId.group("pageId")
                : pageIdQueryParam;
            return pageManager.getPage(Long.parseLong(confluencePageId));
        }
        if (staticPattern.matches() || pageIdQueryParam != null) {
            System.out.println("static pattern Matched ++++++++++++++++++  :  ");
            String confluencePageId = pageIdQueryParam;
            return pageManager.getPage(Long.parseLong(confluencePageId));
        }

        if (spaceAndPage.matches()) {
            System.out.println("space and page Matched ++++++++++++++++++  :  ");
            String spaceKey = spaceAndPage.group("spaceKey");
            // the "+" is automatically added to replace spaces so we need to swap it back
            String pageKey = spaceAndPage.group("page").replace("+", " ");
            System.out.println("spaceKey  :  " + spaceKey);
            System.out.println("pageKey  :  " + pageKey);
            return pageManager.getPage(spaceKey, pageKey);
        }

        return null;

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException{
    
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String uri = httpRequest.getRequestURI();
        System.out.println("uri  :  " + uri);

        ServletContext context = config.getServletContext();
        //System.out.println("contextPath:  " + context.getContextPath());
        // System.out.println("request:  " + response);
        // System.out.println("chain:  " + chain);
        List<String> mandatoryGroups = new ArrayList<String>();
        mandatoryGroups.add("restricted");
        mandatoryGroups.add("restricted2");
        try {
            UserProfile user = this.userManager.getRemoteUser(httpRequest);
            ConfluenceUser loggedInUser = AuthenticatedUserThreadLocal.get();

            Map<String,String[]> paramMap =  request.getParameterMap();
            for (Map.Entry<String,String[]> entry : paramMap.entrySet()) {

                System.out.println(entry.getKey() + " is: " + Arrays.toString(entry.getValue()));
            }


            String confluencePageId = request.getParameter("pageId");   
            String confluenceSpaceId = request.getParameter("spaceId");       
            if (confluenceSpaceId != null) {
                // contentService.find()
                //     .withSpace(confluenceSpaceId)
                //     .fetch
                
            }
            
            Page foundPage = getPage(uri, confluencePageId);
            // if (fail) {
            //     fail = false;
            //     ((HttpServletResponse) response).sendError(HttpServletResponse.SC_NOT_FOUND);
            //     return;
            // }
            if (loggedInUser != null && foundPage != null) {

                System.out.println("_________________________________________________________________________________");
                System.out.println(String.format("Current Date/Time : %tc", new Date()));
                for (String key : paramMap.keySet()) {
                    System.out.println(key + "=" + Arrays.toString(paramMap.get(key)));
                }
                List<String> viewGroups = new ArrayList<String>();

                List<ContentPermissionSet> permissionList = contentPermissionManager.getContentPermissionSets(foundPage, ContentPermission.VIEW_PERMISSION);
                
                List<ContentPermissionSet> updatedPermissionList = new ArrayList<ContentPermissionSet>();
                for (ContentPermissionSet set : updatedPermissionList){
                    
                    for(ContentPermission permission : set){
                        if(permission.isGroupPermission()){
                            viewGroups.add(permission.getGroupName());
                            if (!mandatoryGroups.contains(permission.getGroupName())) {
                                //permission.removeContentPermission
                            }
                        }
                    }
                }
                for (ContentPermissionSet set : permissionList){
                    //if(set.getType().equals(ContentPermission.VIEW_PERMISSION)){
                        for(ContentPermission permission : set){
                            if(permission.isGroupPermission()){
                                viewGroups.add(permission.getGroupName());
                                // if (!mandatoryGroups.contains(permission.getGroupName())) {
                                //     permission.removeContentPermission
                                // }
                            }
                        }
                    //}
                }
                // for group in viewGroups
                //     if mandatoryGroups includes group
                //         if user not in group 
                //             return 404/403

                List<String> userGroups = this.userAccessor.getGroupNamesForUserName(loggedInUser.getName());
                for (String group : viewGroups) {
                    if (mandatoryGroups.contains(group)) {
                        
                        System.out.println("uri: " + uri);
                        System.out.println("mandatory group present: " + group);
                        System.out.println("user groups:  " + Arrays.toString(userGroups.toArray()));

                        if (!userGroups.contains(group)) {
                            System.out.println("user should NOT be able to see");
                            contentPermissionManager.removeAllUserPermissions(loggedInUser);
                            List<String> updatedGroups = userAccessor.getGroupNames(loggedInUser);

                            System.out.println("updated groups: " + Arrays.toString(updatedGroups.toArray()));
                            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_NOT_FOUND);
                            return;
                        }
                        //updatedPermissionList.add(group);
                    }
                }
                // ((HttpServletResponse) response).sendError(HttpServletResponse.SC_NOT_FOUND);
                //         response.setStatus(404);
                //         return;

                System.out.println("did not throw the error");
                System.out.println("================================");
                System.out.println("View groups are :: " + viewGroups);
                System.out.println("USER ==== " + loggedInUser.getName());
                Boolean isVisible = permissionManager.hasPermission(AuthenticatedUserThreadLocal.get(), Permission.VIEW, foundPage);
                System.out.println("visible" + isVisible);

            
            }


        } catch (Exception e) {
            System.out.println("unfortunately, we have errored: " + e);
            //e.printStackTrace();

        }

        if (uri.startsWith("/confluence/rest")) {

            System.out.println("=======================================================");
            System.out.println("is a rest request");
            //System.out.println(Arrays.toString(response.getHeaderNames()));
            HttpServletResponseWrapper responseWrapper = new CharResponseWrapper(httpResponse);


            chain.doFilter(request, responseWrapper);

            System.out.println("after the chain");

            String servletResponse = new String(responseWrapper.toString());
            System.out.println(responseWrapper.getContentType());
            System.out.println("filtered: " + servletResponse);

        } else {
            chain.doFilter(request, response);

        }

            //////////////////////////
            //CHECK FOR API search
            //////////////////////////
            
        // System.out.println("the response is");
        // System.out.println(response);
    }

 
    @Override
    public void destroy(){

    }


}

class CharResponseWrapper extends HttpServletResponseWrapper {
    private CharArrayWriter output; 
   
    public String toString() {
        return output.toString();
    }

    public CharResponseWrapper(HttpServletResponse response){
        super(response);
        System.out.println("we making a new charresponseWrapper");
        output = new CharArrayWriter();
        System.out.println("the response is: " + response.toString());
        System.out.println("the output is: " + output);

    }
    public PrintWriter getWriter(){
        return new PrintWriter(output);
    }
}