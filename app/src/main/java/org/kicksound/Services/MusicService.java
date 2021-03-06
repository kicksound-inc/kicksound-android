package org.kicksound.Services;

import org.kicksound.Models.Mark;
import org.kicksound.Models.Music;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface MusicService {
    @GET("Music/musicByArtist/{id}/{loggedUser}")
    Call<List<Music>> getMusicByArtistId(@Header("Authorization") String authorization,
                                         @Path("id") String id,
                                         @Path("loggedUser") String loggedUser);

    @GET("Music/musicByKind/{id}/{loggedUser}")
    Call<List<Music>> getMusicByKindId(@Header("Authorization") String authorization,
                                         @Path("id") String id,
                                         @Path("loggedUser") String loggedUser);

    @POST("Music/{id}/marks")
    Call<Mark> addMark(@Header("Authorization") String authorization,
                       @Path("id") String id,
                       @Body Mark mark);

    @DELETE("Music/{id}")
    Call<Music> deleteMusic(@Header("Authorization") String authorization,
                            @Path("id") String id);
}
