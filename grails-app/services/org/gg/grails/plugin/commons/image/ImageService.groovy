package org.gg.grails.plugin.commons.image

import org.codehaus.groovy.grails.commons.*

import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.web.context.ServletContextHolder
import org.springframework.web.context.request.RequestContextHolder


class ImageService {


	private static final log = LogFactory.getLog(this)
	
	static transactional = false
	
	def grailsApplication


	/**
	 * Standard image write method write 
	 * 'thumb' and 'image' fileds for passed instance
	 *  
	 * @param instance
	 * @return
	 */
	def write(instance) {
		write(instance,'image')
		write(instance,'thumb')
	}

	/**
	 * Write image uploaded in hard disk
	 * in the folder in config value
	 * image.upload.folder
	 *
	 * @param instance
	 * @param fileType
	 * @return
	 */
	def write(instance,fileType) {

		def fileContent = instance."${fileType}"

		if(!fileContent || !(fileContent.length < 1)){

			try{
				log.debug "write data is not in istance, try to retrive for params]"
				def params = RequestContextHolder.currentRequestAttributes().getParams()
				fileContent = params."$fileType".getBytes()
			}catch(e){
				log.warn "image service write [RequestContextHolder requestewd outside th requst]"
				log.warn "[" + e + "]"
			}
		}

		if(fileContent && fileContent.size() > 0){

			//log.debug "write ${fileType}[" + instance."${fileType}" + "]"

			//retrive base image folder
			def imagesFolder = grailsApplication.config.image.upload.folder

			//getting date image
			def imageDate = instance."${fileType}Date"
			def className = instance.class.simpleName

			def imagesFolderFile = new File("${imagesFolder}")

			if(!imagesFolderFile.exists())
				imagesFolderFile.mkdir()

			try{

				def fileName = "${className}_${instance.id}_${fileType}"

				//create file to write
				def file = new File("${imagesFolder}/$fileName")//_${imageDate}")

				log.debug "write [" + file + "]"

				//copy bytes
				file.setBytes(fileContent)

				//now delete image from cache
				//try to find image in cache
				def basePath = ServletContextHolder.servletContext.getRealPath('/')
				def imageCache = basePath + "/image_cache/"

				def toDelete = new File("$imageCache/$fileName")
				log.debug "write delete from cache[" + toDelete.absolutePath + "]"

				if(toDelete.exists())
					toDelete.delete()

				//if domain class contains a a date field for this field,
				//set this to current date, to have upload time  
			    if(instance."${fileType}Date")
					instance."${fileType}Date" = new Date()
				
				//update image date
				instance.save(flush: true)

			}catch (Exception e) {
				log.error "write [" + e + "]"
			}


		}
	}

	def deleteCache(){
		//now delete image from cache
		//try to find image in cache
		def basePath = ServletContextHolder.servletContext.getRealPath('/')
		def imageCache = basePath + "/image_cache/"
		
		log.debug "deleteCache at folder[" + imageCache + "]"

		try{
			
			new File(imageCache).listFiles().each{
				log.debug "deleteCache [" + it.delete() + "]" 				
			}
			
		}catch(e){
			log.error "delete cache [" + e + "]"
		}

	}
}
