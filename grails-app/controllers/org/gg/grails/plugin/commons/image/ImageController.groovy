package org.gg.grails.plugin.commons.image;


import java.awt.RenderingHints
import java.awt.image.RenderedImage
import java.awt.image.renderable.ParameterBlock

import javax.imageio.ImageIO;
import javax.media.jai.InterpolationBilinear
import javax.media.jai.JAI
import javax.media.jai.PlanarImage

import com.sun.imageio.plugins.jpeg.JPEG;
import com.sun.media.jai.codec.ByteArraySeekableStream
import com.sun.media.jai.codec.JPEGEncodeParam;
import com.sun.media.jai.codec.PNGEncodeParam;
import com.sun.org.apache.xalan.internal.xsltc.compiler.Import;
import com.sun.media.jai.codec.PNGEncodeParam.RGB
import com.sun.media.jai.codec.PNGEncodeParam.Palette
import com.sun.media.jai.codec.SeekableStream
import com.sun.org.apache.bcel.internal.generic.NEW;


class ImageController {

	static final IMAGE_WIDTH = 480
	static final IMAGE_HEIGHT = 360

	static final THUMB_WIDTH = 90
	static final THUMB_HEIGHT = 70

	static defaultAction ='show'

	/** 
	 * This action load image for class type and field type
	 * 
	 * If image exesit in cache load image from cache, 
	 * while if image not is in cache SCALE the image and save 
	 * it in cache.
	 * 
	 */
	def show = {

		//loads the class with a name and assigns obj a new instance created of the same object
		params.classname = params.classname.capitalize()

		//instace class from url parameter
		def classInstance = Class.forName(grailsApplication.config.app.package+"."+"${params.classname}",true,Thread.currentThread().contextClassLoader);

		//lod dynamicalli object from type and id
		def object = classInstance.get( params.id )

		//		if(params.mime){
		//			response.setContentType(params.mime)
		//		}else{
		response.setContentType("image/jpeg")
		//		}

		//try to find image in cache
		def basePath = request.getSession().getServletContext().getRealPath("/")
		def imageCache = basePath + "/image_cache/"

		def imageName = params.classname + "_" + params.id +"_"  + params.fieldName // + "_" + object."${params.fieldName}Date"?.getTime()
		log.debug("show cache imageName[" + imageName +"]");

		File imageDir = new File(imageCache);

		byte[] image;
		def isFromCache = false;

		//se la diorectory delle immagini non esiste la creo
		if(!imageDir.exists()){
			imageDir.mkdir()
		}else{

			File imageFile = new File(imageCache+imageName)

			if(imageFile.exists()){

				log.debug("show loaded from cache[" + imageFile +"]");
				isFromCache = true;

			}else{

				def originalPath = grailsApplication.config.image.upload.folder
				imageFile = new File("${originalPath}/${imageName}")
				log.debug("show image not in cache, load from file[" + imageFile +"]");

			}

			if(imageFile.exists()){
				image = new byte[(int)imageFile.length()]
				new FileInputStream(imageFile).read(image)
			}else{

				log.debug("show image not in disk, try to load from object[" + object +"]");

				try{
					image = object."${params.fieldName}"
				}catch(Exception e){
					log.warn "image exception [" + e + "]"
				}

			}

		}

		if(image){

			if(!isFromCache){
				//now search image in cache : TODO
				//if image notincache in scale and crop image
				//save it on cache and return it
				request.getSession().getServletContext().getRealPath("/")

				def height = IMAGE_HEIGHT
				def width = IMAGE_WIDTH

				//this is only for backward compatibility
				if(params.fieldName == "thumb"){
					height = THUMB_HEIGHT
					width = THUMB_WIDTH
				}else {
				
					def fullFieldName = grailsApplication.config.imageService."$params.fieldName"

					if(fullFieldName.height)
						height = fullFieldName.height
					if(fullFieldName.width)
						width = fullFieldName.width

				}

				image = scaleCropImage(image, width, height)


				File imageFile = new File(imageCache+imageName)

				new FileOutputStream(imageFile).write(image)

			}

			response.outputStream << image
		}else{
			redirect(uri: "/images/default.png")
		}
	}

	//	private byte[] getCroppedImage(image){
	//
	//		def height = IMAGE_HEIGHT
	//		def width = IMAGE_WIDTH
	//
	//		if(grailsApplication.config.imageService.image.height)
	//			height = grailsApplication.config.imageService.image.height
	//		if(grailsApplication.config.imageService.image.width)
	//			width = grailsApplication.config.imageService.image.width
	//
	//		return scaleCropImage(image, width, height)
	//	}
	//
	//	private byte[] getCroppedThumb(thumb){
	//
	//		def height = THUMB_HEIGHT
	//		def width = THUMB_WIDTH
	//
	//		if(grailsApplication.config.imageService.thumb.height)
	//			height = grailsApplication.config.imageService.thumb.height
	//		if(grailsApplication.config.imageService.thumb.width)
	//			width = grailsApplication.config.imageService.thumb.width
	//
	//		return scaleCropImage(thumb, width, height)
	//	}

	private byte[] scaleCropImage(image,width,height){


		SeekableStream seekableImageStream = new ByteArraySeekableStream(image)
		PlanarImage pi  = JAI.create("Stream", seekableImageStream);

		float scale = 1.0F

		//caso immagine più grande
		if(pi.getHeight() > height && pi.getWidth() > width){


			float Hratio =  height /  pi.getHeight()
			float Wratio =  width / pi.getWidth()

			scale = Hratio > Wratio ? Hratio : Wratio

		}else{

			float Hratio =  height / pi.getHeight()
			float Wratio =  width / pi.getWidth()

			scale = Hratio > Wratio ? Hratio : Wratio
		}

		ParameterBlock pb = new ParameterBlock();

		pb.addSource(pi); // The source image

		pb.add(scale);          // The xScale
		pb.add(scale);          // The yScale

		pb.add(0.0F);           // The x translation
		pb.add(0.0F);           // The y translation

		pb.add(new InterpolationBilinear()); // The interpolation

		RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_SPEED);

		RenderedImage ri = JAI.create("scale",pb,qualityHints);

		pb = new ParameterBlock();
		pb.addSource(ri);

		float xShift = ((ri.getWidth() - width) / 2)
		float yShift = ((ri.getHeight() - height) / 2)

		pb.add((float)xShift);
		pb.add((float)yShift);

		pb.add((float)width);
		pb.add((float)height);

		PlanarImage result  = JAI.create("crop",pb,null);


		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		//		 write
		ImageIO.write(result.getAsBufferedImage(), "jpeg" /* "png" "jpeg" ... format desired */,
				baos );


		// close
		baos.flush();
		byte[] resultImageAsRawBytes = baos.toByteArray();
		baos.close();

		return resultImageAsRawBytes;



	}
}