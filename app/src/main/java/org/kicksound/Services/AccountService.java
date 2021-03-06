package org.kicksound.Services;

import org.kicksound.Models.Account;
import org.kicksound.Models.Event;
import org.kicksound.Models.Login;
import org.kicksound.Models.Logout;
import org.kicksound.Models.Music;
import org.kicksound.Models.Playlist;
import org.kicksound.Models.ResetPassword;
import org.kicksound.Models.Ticket;

import java.util.Date;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AccountService {
    @GET("accounts/me")
    Call<Account> accessTokenExist(@Header("Authorization") String authorization);

    @GET("accounts/{id}")
    Call<Account> getUserById(@Header("Authorization") String authorization, @Path("id") String id);

    @GET("accounts")
    Call<List<Account>> getUsersByUserName(@Header("Authorization") String authorization,
                                           @Query("filter[where][and][0][username][like]=") String username,
                                           @Query("filter[where][and][1][type][neq]=") String classicUser,
                                           @Query("filter[where][and][2][id][neq]=") String userId);

    @GET("Photos/{container}/download/{file}")
    Call<ResponseBody> downloadFile(
            @Header("Authorization") String authorization,
            @Path("container") String container,
            @Path("file") String file
    );

    @GET("accounts/{id}/following")
    Call<List<Account>> getFollowedUsers(@Header("Authorization") String authorization,
                                 @Path("id") String id);

    @GET("accounts/{id}/following/{fk}")
    Call<Account> getFollowedUserById(@Header("Authorization") String authorization,
                                      @Path("id") String id,
                                      @Path("fk") String fk);

    @GET("accounts/{id}/highlight/{fk}")
    Call<Account> getHighlightUserById(@Header("Authorization") String authorization,
                                      @Path("id") String id,
                                      @Path("fk") String fk);

    @GET("accounts/{id}/events")
    Call<List<Event>> getUserEvents(@Header("Authorization") String authorization,
                                    @Path("id") String id,
                                    @Query("filter[order]=") String by,
                                    @Query("filter[where][date][gt]=") Date date);

    @GET("accounts/{id}/events/{fk}")
    Call<Event> getEventById(@Header("Authorization") String authorization,
                                    @Path("id") String id,
                                    @Path("fk") String ifk);

    @GET("accounts/{id}/playlists")
    Call<List<Playlist>> getPlaylist(@Header("Authorization") String authorization,
                                  @Path("id") String id);

    @GET("accounts/{id}/eventParticipation")
    Call<List<Event>> getEventParticipation(@Header("Authorization") String authorization,
                                            @Path("id") String id);

    @GET("accounts/{id}/eventByFollowedUser")
    Call<List<Event>> getEventByFollowedUser(@Header("Authorization") String authorization,
                                            @Path("id") String id);

    @GET("accounts/{id}/favoriteMusicsAndOwner")
    Call<List<Music>> getArtistFavoriteMusics(@Header("Authorization") String authorization,
                                              @Path("id") String id);

    @GET("accounts/{id}/unknownArtistsByArtistFollowed")
    Call<List<Account>> getUnknownArtistsByArtistFollowed(@Header("Authorization") String authorization,
                                                          @Path("id") String id);


    @PUT("accounts/{id}/following/rel/{fk}")
    Call<Account> followUser(@Header("Authorization") String authorization,
                                      @Path("id") String id,
                                      @Path("fk") String fk);

    @PUT("accounts/{id}/highlight/rel/{fk}")
    Call<Account> highlightUser(@Header("Authorization") String authorization,
                             @Path("id") String id,
                             @Path("fk") String fk);

    @PUT("accounts/{id}/favoriteMusics/rel/{fk}")
    Call<Music> addMusicToFavorites(@Header("Authorization") String authorization,
                                    @Path("id") String id,
                                    @Path("fk") String fk);

    @PUT("accounts/{id}/events/{fk}")
    Call<Event> updateEvent(@Header("Authorization") String authorization,
                            @Path("id") String id,
                            @Path("fk") String fk,
                            @Body Event event);

    @PUT("accounts/{id}/playlists/{nk}/musics/rel/{fk}")
    Call<Music> addMusicToPlaylist(
            @Header("Authorization") String authorization,
            @Path("id") String accountId,
            @Path("nk") String playlistId,
            @Path("fk") String musicId
    );

    @DELETE("accounts/{id}/playlists/{nk}/musics/rel/{fk}")
    Call<Music> deleteMusicToPlaylist(
            @Header("Authorization") String authorization,
            @Path("id") String accountId,
            @Path("nk") String playlistId,
            @Path("fk") String musicId
    );

    @DELETE("accounts/{id}")
    Call<Account> deleteAccount(@Header("Authorization") String authorization,
                                @Path("id") String id);

    @DELETE("accounts/{id}/highlight/rel/{fk}")
    Call<Account> deleteHighlightByUserById(@Header("Authorization") String authorization,
                                           @Path("id") String id,
                                           @Path("fk") String fk);

    @DELETE("accounts/{id}/following/rel/{fk}")
    Call<Account> unfollowUser(@Header("Authorization") String authorization,
                             @Path("id") String id,
                             @Path("fk") String fk);

    @DELETE("accounts/{id}/playlists/{fk}")
    Call<Playlist> deletePlaylist(@Header("Authorization") String authorization,
                                  @Path("id") String id,
                                  @Path("fk") String fk);

    @DELETE("accounts/{id}/favoriteMusics/rel/{fk}")
    Call<Music> deleteMusicToFavorites(@Header("Authorization") String authorization,
                                    @Path("id") String id,
                                    @Path("fk") String fk);

    @POST("accounts")
    Call<Account> createAccount(@Body Account account);

    @POST("accounts/login")
    Call<Login> loginAccount(@Body Login login);

    @POST("accounts/logout")
    Call<Logout> logout(@Header("Authorization") String authorization);

    @POST("accounts/reset-password")
    Call<ResetPassword> resetPassword(@Header("Authorization") String authorization, @Body ResetPassword resetPassword);

    @POST("accounts/{id}/events")
    Call<Event> createEvent(@Header("Authorization") String authorization, @Path("id") String id, @Body Event event);

    @POST("accounts/{id}/playlists")
    Call<Playlist> createPlaylist(@Header("Authorization") String authorization,
                                  @Path("id") String id,
                                  @Body Playlist playlist);

    @POST("accounts/{id}/artistMusic")
    Call<Music> createMusic(@Header("Authorization") String authorization,
                            @Path("id") String id,
                            @Body Music music);

    @Multipart
    @POST("Photos/{container}/upload")
    Call<ResponseBody> uploadFile(
            @Header("Authorization") String authorization,
            @Part MultipartBody.Part file,
            @Path("container") String container

    );

    @PATCH("accounts/{id}")
    Call<Account> updatePictureName(@Header("Authorization") String authorization, @Path("id") String id, @Body Account account);
}
