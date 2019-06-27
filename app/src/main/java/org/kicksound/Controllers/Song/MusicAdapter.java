package org.kicksound.Controllers.Song;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import org.kicksound.Controllers.Playlist.AddMusicToPlayList;
import org.kicksound.Models.Music;
import org.kicksound.R;
import org.kicksound.Services.AccountService;
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

    private Runnable timeRunnable;
    private Handler mHandler=new Handler();
    private View view;

    public MusicAdapter(List<Music> musicList, Activity activity, Context context, MediaPlayer mediaPlayer, Handler seekbarUpdateHandler, Runnable updateSeekbar, SeekBar seekBar, TextView musicNameStarted, ImageButton forward, ImageButton rewind, ProgressBar progressBar) {
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
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.music_item, parent, false);
        return new MusicAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.itemMusicName.setText(musicList.get(position).getTitle());
        holder.itemArtistName.setText(musicList.get(position).getAccounts().getUsername());
        holder.musicItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchMusic(position);
                forward(position);
                rewind(position);
                onPressedMusic(holder, position);
            }
        });
        displayFavoriteStar(holder, position);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void onPressedMusic(final ViewHolder holder, final int position) {
        timeRunnable=new Runnable(){
            @Override
            public void run() {
                HandleIntent.redirectToAnotherActivityWithExtra(context, AddMusicToPlayList.class, view, "musicId", musicList.get(position).getId());
            }
        };

        holder.musicItem.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                view = v;
                switch ( event.getAction() ) {
                    case MotionEvent.ACTION_DOWN:
                        mHandler.postDelayed(timeRunnable, 500);
                        break;
                    case MotionEvent.ACTION_UP:
                        mHandler.removeCallbacks(timeRunnable);
                        break;
                }
                return true;
            }
        });
    }

    private void displayFavoriteStar(ViewHolder holder, int position) {
        if(musicList.get(position).getAccountWhoLike().isEmpty()) {
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
        setMusicTitle(position);
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

    private void setMusicTitle(int position) {
        musicNameStarted.setText(musicList.get(position).getTitle());
        musicNameStarted.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        musicNameStarted.setSingleLine(true);
        musicNameStarted.setMarqueeRepeatLimit(5);
        musicNameStarted.setSelected(true);
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView itemMusicName;
        TextView itemArtistName;
        CardView musicItem;
        ConstraintLayout constraintBackground;
        ImageButton whiteStar;
        ImageButton yellowStar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemMusicName = itemView.findViewById(R.id.itemMusicName);
            itemArtistName = itemView.findViewById(R.id.itemArtistName);
            musicItem = itemView.findViewById(R.id.item_card_music);
            constraintBackground = itemView.findViewById(R.id.constraintBackground);
            whiteStar = itemView.findViewById(R.id.whiteStar);
            yellowStar = itemView.findViewById(R.id.yellowStar);
        }
    }
}