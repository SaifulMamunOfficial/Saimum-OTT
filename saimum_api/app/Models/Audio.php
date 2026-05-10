<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Audio extends Model
{
    use HasFactory;

    protected $table = 'tbl_audio';
    public $timestamps = false;

    protected $casts = [
        'total_views' => 'integer',
        'total_download' => 'integer',
    ];

    protected $fillable = [
        'audio_title',
        'audio_url',
        'audio_url_high',
        'audio_url_low',
        'audio_thumbnail',
        'audio_artist',
        'audio_description',
        'album_id',
        'cat_id',
        'total_views',
        'total_download',
        'rate_avg',
        'audio_status'
    ];

    public function album()
    {
        return $this->belongsTo(Album::class, 'album_id', 'aid');
    }

    public function category()
    {
        return $this->belongsTo(Category::class, 'cat_id', 'cid');
    }
}
