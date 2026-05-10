<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Artist extends Model
{
    use HasFactory;

    protected $table = 'tbl_artist';
    protected $primaryKey = 'id';
    public $timestamps = false;

    protected $fillable = [
        'artist_name',
        'artist_image',
        'status'
    ];
}
