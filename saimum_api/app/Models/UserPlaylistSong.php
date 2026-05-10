<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class UserPlaylistSong extends Model
{
    use HasFactory;

    protected $fillable = ['playlist_id', 'song_id', 'order'];

    public function playlist()
    {
        return $this->belongsTo(UserPlaylist.class, 'playlist_id');
    }
}
