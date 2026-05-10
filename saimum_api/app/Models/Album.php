<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Album extends Model
{
    use HasFactory;

    protected $table = 'tbl_album';
    protected $primaryKey = 'aid';
    public $timestamps = false;

    protected $fillable = [
        'album_name',
        'album_image',
        'artist_ids',
        'catid',
        'status'
    ];

    public function audios()
    {
        return $this->hasMany(Audio::class, 'album_id', 'aid');
    }
}
