package org.gg.grails.plugin.commons.image

class ImageTagLib {

	/**
	 * Create image tag for thumbnail
	 */
	def image = {
		attrs, body ->

		log.debug "image [" + attrs.bean.class.simpleName.toLowerCase() + "]"

		if(attrs.bean && attrs.bean.id){
			def link = createLink(mapping: 'image',
			params: [controller : 'image' ,
				classname: attrs.bean.class.simpleName.toLowerCase() ,
				fieldName: attrs.type,
				id: attrs.bean.id])

			log.debug "link [" + link + "]"

			try{
				link += "?" + attrs.bean."${attrs.type}Date".getTime()
			}catch(e){
				log.warn "exceprion [" + e + "]"
			}
			
			log.debug "link [" + link + "]"

			out << """
					<a	href="$link" target="_blank">
						 <img style="height: 50px;" src="${link}" /> 
				  	</a>
					"""
			
		}
	}

	/**
	 * Create image link
	 */
	def imageLink = {
		attrs, body ->

		def link = ""

		if(attrs.bean && attrs.bean.id){
			link = createLink(mapping: 'image',
			params: [controller : 'image' ,
				classname: attrs.bean.class.simpleName.toLowerCase() ,
				fieldName: attrs.type,
				id: attrs.bean.id])
		}

		out << link
	}
}
