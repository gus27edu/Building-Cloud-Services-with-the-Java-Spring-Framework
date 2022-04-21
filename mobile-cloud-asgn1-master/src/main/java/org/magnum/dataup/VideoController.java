/*
 * 
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.magnum.dataup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.io.IOUtils;
import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.magnum.dataup.model.VideoStatus.VideoState;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import retrofit.client.Response;
import retrofit.mime.TypedFile;

@Controller
public class VideoController implements VideoSvcApi{
	
	// Una lista en memoria que el servlet usa para almacenar los videos enviados por los clientes.
	private List<Video> videos = new CopyOnWriteArrayList<Video>();
	private long ids=1;

	/**
	 * This endpoint in the API returns a list of the videos that have
	 * been added to the server. The Video objects should be returned as
	 * JSON. 
	 * 
	 * To manually test this endpoint, run your server and open this URL in a browser:
	 * http://localhost:8080/video
	 * @return
	 */
	@Override
	@RequestMapping(value=VIDEO_SVC_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<Video> getVideoList() {
		return videos;
	}
	
	/**
	 * This endpoint allows clients to add Video objects by sending POST requests
	 * that have an application/json body containing the Video object information.
	 * @return
	 */
	@Override
	@RequestMapping(value=VIDEO_SVC_PATH, method=RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video v) {
		v.setId(ids++);
		videos.add(v);
		return v;
	}
 
	/**
	 * This endpoint allows clients to set the mpeg video data for previously
	 * added Video objects by sending multipart POST requests to the server.
	 * The URL that the POST requests should be sent to includes the ID of the
	 * Video that the data should be associated with (e.g., replace {id} in
	 * the url /video/{id}/data with a valid ID of a video, such as /video/1/data
	 * -- assuming that "1" is a valid ID of a video). 
	 * 
	 * @return
	 */
	/* 	public void testAddVideoData() throws Exception {
			Video received = videoSvc.addVideo(video);
			VideoStatus status = videoSvc.setVideoData<=====>(received.getId(), new TypedFile(received.getContentType(), testVideoData));
			assertEquals(VideoState.READY, status.getState());
			
			Response response = videoSvc.getData(received.getId());
			assertEquals(200, response.getStatus());
			
			InputStream videoData = response.getBody().in();
			byte[] originalFile = IOUtils.toByteArray(new FileInputStream(testVideoData));
			byte[] retrievedFile = IOUtils.toByteArray(videoData);
			assertTrue(Arrays.equals(originalFile, retrievedFile));
	}*/
	/// Voy a recibir un id y un videoData, lo tengo que agregar a un video de la lista que tenga el mismo id
	/// Que sea multipart, el id de un video cargado, el cliente envia en el path la seccion DATA e ID. La API 
    /// ya me secciona el path, y me lo envia al VideoController listo.
	/// ¨The binary mpeg data for the video should be provided in a multipart request as a part with the key "data"¨
	
	@Override
	@RequestMapping(value=VIDEO_DATA_PATH, method=RequestMethod.POST)
	public @ResponseBody VideoStatus setVideoData(@PathVariable("id") long id, @RequestParam("data") TypedFile videoData) {
		

		
		VideoStatus i = null;
		try {
			Video stored = null;
			// Searchs a video on the List with the same id and copies it into a Video object. Then it´s used
			// in the VideoFileManager saveVideoData method:
			for(Video v: videos) {
				if(v.getId()==id) {
					stored = v;
				}
			}
			// VideoFIleManager instance & invokinq saveVideoData():
			VideoFileManager videoDataMgr = VideoFileManager.get();
			videoDataMgr.saveVideoData(stored, videoData.in());
			
			// Creating VideoStatus instance to return it:
			i = new VideoStatus(VideoState.READY);

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return i;
	}


	@Override
	@RequestMapping(value=VIDEO_DATA_PATH , method=RequestMethod.GET )
	public Response getData(long id) {
		// TODO Auto-generated method stub
		return null;
	}

}
