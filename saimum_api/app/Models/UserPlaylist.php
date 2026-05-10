<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class UserPlaylist extends Model
{
    use HasFactory;

    protected $fillable = ['user_id', 'name', 'image', 'is_public'];

    public function songs()
    {
        return $this->hasMany(UserPlaylistSong::class, 'playlist_id');
    }
}
