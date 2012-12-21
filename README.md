This is a simple plugin for manage image fields.
Automatically save original image, create scaled version 
and include tags to show image and create url to that.  

#How to use
Install the plugin on your Grails project

##Configure

In UrlMappings.groovy file add a named mapping like that:
    
    name image : "/image/$classname/$id/$fieldName"{
    	controller="image"
    }
    
This will be the url in wich you can access your images,
* classname is the domain class in wich you have the image field  
* id is the id of the domain instance
* fieldname is the name of the image field

In config.groovy file add this options:

    //package of domain class
    app.package = 'com.gc.local.cms'

    //folder for orginal files
    image.upload.folder = "/usr/share/tomcat7/appspa_images"
    

##Use
Create field in domain class: 

    byte[] image
    byte[] thumb
    
Fileds 'thumb' and 'image' are standard fields, 
but you can use every field name you want.

fields must be transient to avoid that grails create this field in database.

    static transients = ['image' , 'thumb']
    
moreover on domain class you can add a Date fields, with name FIELDNAMEDate,

    Date thumbDate
  	Date imageDate
  	
to have upload date saved on your database.

To save image field on controller

    imageService.write(serviceInstance ,'FIELDNAME')
    
There is a shortcut wich allows you to save standard image fields 'image' and 'thumb'
if you used them 
  
    imageService.write(serviceInstance)

You can configure height and width for fields in config.groovy

    imageService.image.width    //standard 480
    imageService.image.height   //standard 360

    imageService.thumb.width	  //standard 90
    imageService.thumb.height	  //standard 70
    
    imageService.FIELDNAME.width	//standard 480
    imageService.FIELDNAME.height	//standard 360
 
These are available gsp tags: 

This tag create html img tag 

    <g:image bean="${doaminInstance}" type="thumb" />

This tag create only link to image

    <g:imageLink bean="${domainInstance}" type="thumb" />
