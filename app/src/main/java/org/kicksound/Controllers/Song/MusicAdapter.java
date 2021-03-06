package org.kicksound.Controllers.Song;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import org.kicksound.Controllers.Playlist.AddMusicToPlayList;
import org.kicksound.Controllers.Playlist.Playlists;
import org.kicksound.Models.Music;
import org.kicksound.Models.Playlist;
import org.kicksound.R;
import org.kicksound.Services.AccountService;
import org.kicksound.Services.MusicService;
import org.kicksound.Utils.Class.HandleAccount;
import org.kicksound.Utils.Class.HandleIntent;
import org.kicksound.Utils.Class.MusicUtil;
import org.kicksound.Utils.Class.RetrofitManager;

import java.util.List;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.ViewHolder> {

    private static final int PLAYLIST = 0;
    private static final int MARK = 1;
    private static final int DELETE = 2;
    private static final int DELETE_FROM_PLAYLIST = 2;
    private List<Music> musicList;
    private Activity activity;
    private Context context;
    private MediaPlayer mediaPlayer;
    private Handler seekbarUpdateHandler;
    private Runnable updateSeekbar;
    private SeekBar seekBar;
    private TextView musicNameStarted;
    private ImageButton forward;
    private ImageButton rewind;
    private ProgressBar progressBar;
    private String playlistId;

    private Runnable timeRunnable;
    private Handler mHandler=new Handler();
    private View view;

    public MusicAdapter(List<Music> musicList, Activity activity, Context context, MediaPlayer mediaPlayer, Handler seekbarUpdateHandler, Runnable updateSeekbar, SeekBar seekBar, TextView musicNameStarted, ImageButton forward, ImageButton rewind, ProgressBar progressBar, String playlistId) {
        this.musicList = musicList;
        this.activity = activity;
        this.context = context;
        this.mediaPlayer = mediaPlayer;
        this.seekbarUpdateHandler = seekbarUpdateHandler;
        this.updateSeekbar = updateSeekbar;
        this.seekBar = seekBar;
        this.musicNameStarted = musicNameStarted;
        this.forward = forward;
        this.rewind = rewind;
        this.progressBar = progressBar;
        this.playlistId = playlistId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.music_item, parent, false);
        return new MusicAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        setMusicTitle(position, holder.itemMusicName);
        holder.itemArtistName.setText(musicList.get(position).getAccounts().getUsername());
        holder.musicItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchMusic(position);
                forward(position);
                rewind(position);
            }
        });
        setDotsMenuItems(position, holder);
        displayFavoriteStar(holder, position);
        holder.ratingBar.setRating(musicList.get(position).getMark());
    }

    private void setDotsMenuItems(final int position, ViewHolder holder) {
        holder.dotsMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDotsMenu(position, v);
            }
        });
    }

    private void resetPlayer() {
        seekbarUpdateHandler.removeCallbacks(updateSeekbar);
        updateSeekbar = null;
        seekbarUpdateHandler = null;
        mediaPlayer.stop();
        mediaPlayer.release();
    }

    private void setDotsMenu(final int position, final View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        if(playlistId != null) {
            builder.setTitle(musicList.get(position).getTitle())
                    .setItems(R.array.playlistMusicArray, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if(which == PLAYLIST) {
                                seekbarUpdateHandler.removeCallbacks(updateSeekbar);
                                resetPlayer();
                                activity.finish();
                                HandleIntent.redirectToAnotherActivityWithExtra(context, AddMusicToPlayList.class, v, "musicId", musicList.get(position).getId());
                            } else if( which == MARK) {
                                resetPlayer();
                                activity.finish();
                                HandleIntent.redirectToAnotherActivityWithExtra(context, RateMusic.class, v, "musicId", musicList.get(position).getId());
                            } else if( which == DELETE_FROM_PLAYLIST ) {
                                resetPlayer();
                                deleteMusicFromPlaylist(position);
                                activity.finish();
                                HandleIntent.redirectToAnotherActivityWithExtra(context, FavoriteMusics.class, v, "playlistId", playlistId);

                            }
                        }
                    });
        } else if(HandleAccount.userAccount.getId().equals(musicList.get(position).getAccountId())){
            builder.setTitle(musicList.get(position).getTitle())
                    .setItems(R.array.musicArrayTwo, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if(which == PLAYLIST) {
                                resetPlayer();
                                activity.finish();
                                HandleIntent.redirectToAnotherActivityWithExtra(context, AddMusicToPlayList.class, v, "musicId", musicList.get(position).getId());
                            } else if(which == MARK) {
                                resetPlayer();
                                activity.finish();
                                HandleIntent.redirectToAnotherActivityWithExtra(context, RateMusic.class, v, "musicId", musicList.get(position).getId());
                            } else if(which == DELETE) {
                                resetPlayer();
                                deleteMusic(musicList.get(position).getId());
                                activity.finish();
                            }
                        }
                    });
        } else {
            builder.setTitle(musicList.get(position).getTitle())
                    .setItems(R.array.musicArray, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if(which == PLAYLIST) {
                                resetPlayer();
                                activity.finish();
                                HandleIntent.redirectToAnotherActivityWithExtra(context, AddMusicToPlayList.class, v, "musicId", musicList.get(position).getId());
                            } else if(which == MARK) {
                                resetPlayer();
                                activity.finish();
                                HandleIntent.redirectToAnotherActivityWithExtra(context, RateMusic.class, v, "musicId", musicList.get(position).getId());
                            }
                        }
                    });
        }

        builder.create();
        builder.show();
    }

    private void deleteMusic(String id) {
        RetrofitManager.getInstance().getRetrofit().create(MusicService.class)
                .deleteMusic(
                        HandleAccount.userAccount.getAccessToken(),
                        id
                ).enqueue(new Callback<Music>() {
            @Override
            public void onResponse(Call<Music> call, Response<Music> response) {
                Toasty.success(context, context.getString(R.string.deleteMusic), Toast.LENGTH_SHORT, true).show();
            }

            @Override
            public void onFailure(Call<Music> call, Throwable t) {
                Toasty.error(context, context.getString(R.string.connexion_error), Toast.LENGTH_SHORT, true).show();
            }
        });
    }

    private void deleteMusicFromPlaylist(int position) {
        RetrofitManager.getInstance().getRetrofit().create(AccountService.class)
                .deleteMusicToPlaylist(
                        HandleAccount.userAccount.getAccessToken(),
                        HandleAccount.userAccount.getId(),
                        playlistId,
                        musicList.get(position).getId()
                ).enqueue(new Callback<Music>() {
            @Override
            public void onResponse(Call<Music> call, Response<Music> response) {
                Toasty.success(context, context.getString(R.string.musicRemovedFromPlaylist), Toast.LENGTH_SHORT, true).show();
            }

            @Override
            public void onFailure(Call<Music> call, Throwable t) {
                Toasty.error(context, context.getString(R.string.connexion_error), Toast.LENGTH_SHORT, true).show();
            }
        });
    }

    private void displayFavoriteStar(ViewHolder holder, int position) {
        if(musicList.get(position).getAccountWhoLike() == null || musicList.get(position).getAccountWhoLike().isEmpty()) {
            setWhiteStarVisibleAndYellowStarGone(holder);
            addFavorite(holder, position);
        } else {
            setYellowStarVisibleAndWhiteStarGone(holder);
            deleteFavorites(holder, position);
        }
    }

    private void addFavorite(final ViewHolder holder, final int position) {
        holder.whiteStar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RetrofitManager.getInstance().getRetrofit().create(AccountService.class)
                        .addMusicToFavorites(
                                HandleAccount.userAccount.getAccessToken(),
                                HandleAccount.userAccount.getId(),
                                musicList.get(position).getId()
                        ).enqueue(new Callback<Music>() {
                    @Override
                    public void onResponse(Call<Music> call, Response<Music> response) {
                        if(response.code() == 200) {
                            setYellowStarVisibleAndWhiteStarGone(holder);
                            deleteFavorites(holder, position);
                            Toasty.success(context, context.getString(R.string.musicAddedToFavorites), Toast.LENGTH_SHORT, true).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Music> call, Throwable t) {
                        Toasty.error(context, context.getString(R.string.connexion_error), Toast.LENGTH_SHORT, true).show();
                    }
                });
            }
        });
    }

    private void deleteFavorites(final ViewHolder holder, final int position) {
        holder.yellowStar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RetrofitManager.getInstance().getRetrofit().create(AccountService.class)
                        .deleteMusicToFavorites(
                                HandleAccount.userAccount.getAccessToken(),
                                HandleAccount.userAccount.getId(),
                                musicList.get(position).getId()
                        ).enqueue(new Callback<Music>() {
                    @Override
                    public void onResponse(Call<Music> call, Response<Music> response) {
                        if(response.code() == 204) {
                            setWhiteStarVisibleAndYellowStarGone(holder);
                            addFavorite(holder, position);
                            Toasty.success(context, context.getString(R.string.musicRemoveFromFavorites), Toast.LENGTH_SHORT, true).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Music> call, Throwable t) {
                        Toasty.error(context, context.getString(R.string.connexion_error), Toast.LENGTH_SHORT, true).show();
                    }
                });
            }
        });
    }

    private void setYellowStarVisibleAndWhiteStarGone(ViewHolder holder) {
        holder.yellowStar.setVisibility(View.VISIBLE);
        holder.whiteStar.setVisibility(View.GONE);
    }

    private void setWhiteStarVisibleAndYellowStarGone(ViewHolder holder) {
        holder.whiteStar.setVisibility(View.VISIBLE);
        holder.yellowStar.setVisibility(View.GONE);
    }

    private void launchMusic(int position) {
        MusicUtil.loadMusic(musicList.get(position).getLocation(), context, activity, mediaPlayer, seekbarUpdateHandler, updateSeekbar, seekBar, progressBar);
        setMusicTitle(position, musicNameStarted);
        songIsCompleted(position);
    }

    private void songIsCompleted(final int position) {
        MusicUtil.songIsCompleted(mediaPlayer, new MediaPlayer.OnCompletionListener() {
            final int[] currentPosition = {position};
            @Override
            public void onCompletion(MediaPlayer mp) {
                if(musicList.size() - 1 > currentPosition[0]) {
                    currentPosition[0] += 1;
                    launchMusic(currentPosition[0]);
                } else {
                    currentPosition[0] = 0;
                    launchMusic(currentPosition[0]);
                }
                forward(currentPosition[0]);
                rewind(currentPosition[0]);
            }
        });
    }

    private void forward(final int position) {
        final int[] currentPosition = {position};
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(musicList.size() - 1 > currentPosition[0]) {
                    currentPosition[0] += 1;
                    launchMusic(currentPosition[0]);
                } else {
                    currentPosition[0] = 0;
                    launchMusic(currentPosition[0]);
                }
                rewind(currentPosition[0]);
            }
        });
    }
    private void rewind(final int position) {
        final int[] currentPosition = {position};
        rewind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentPosition[0] == 0) {
                    currentPosition[0] = musicList.size() - 1;
                    launchMusic(currentPosition[0]);
                } else {
                    currentPosition[0] -= 1;
                    launchMusic(currentPosition[0]);
                }
                forward(currentPosition[0]);
            }
        });
    }

    private void setMusicTitle(int position, TextView musicName) {
        musicName.setText(musicList.get(position).getTitle());
        musicName.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        musicName.setSingleLine(true);
        musicName.setMarqueeRepeatLimit(5);
        musicName.setSelected(true);
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView itemMusicName;
        TextView itemArtistName;
        CardView musicItem;
        ConstraintLayout constraintBackground;
        ImageButton whiteStar;
        ImageButton yellowStar;
        ImageButton dotsMenu;
        RatingBar ratingBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemMusicName = itemView.findViewById(R.id.itemMusicName);
            itemArtistName = itemView.findViewById(R.id.itemArtistName);
            musicItem = itemView.findViewById(R.id.item_card_music);
            constraintBackground = itemView.findViewById(R.id.constraintBackground);
            whiteStar = itemView.findViewById(R.id.whiteStar);
            yellowStar = itemView.findViewById(R.id.yellowStar);
            dotsMenu = itemView.findViewById(R.id.dotsMenu);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }
}
