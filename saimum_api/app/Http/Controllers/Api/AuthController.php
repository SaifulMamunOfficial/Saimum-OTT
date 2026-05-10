<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Validator;

class AuthController extends Controller
{
    public function register(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'user_name' => 'required|string|max:255',
            'user_email' => 'required|string|email|max:255|unique:tbl_users',
            'user_password' => 'required|string|min:6',
        ]);

        if ($validator->fails()) {
            return response()->json(['success' => '0', 'MSG' => $validator->errors()->first()], 422);
        }

        $user = User::create([
            'user_name' => $request->user_name,
            'user_email' => $request->user_email,
            'user_password' => Hash::make($request->user_password),
            'user_type' => 'Normal',
            'status' => '1',
            'role' => $request->role ?? 'user',
            'student_id' => $request->student_id,
        ]);

        $token = $user->createToken('auth_token')->plainTextToken;

        return response()->json([
            'success' => '1',
            'MSG' => 'Registration successful',
            'user_id' => $user->id,
            'access_token' => $token,
            'token_type' => 'Bearer',
        ]);
    }

    public function login(Request $request)
    {
        $user = User::where('user_email', $request->user_email)->first();

        if (!$user || !Hash::check($request->user_password, $user->user_password)) {
            return response()->json([
                'success' => '0',
                'MSG' => 'Invalid login credentials'
            ], 401);
        }

        $token = $user->createToken('auth_token')->plainTextToken;

        return response()->json([
            'success' => '1',
            'MSG' => 'Login successful',
            'user_id' => $user->id,
            'user_name' => $user->user_name,
            'role' => $user->role,
            'student_id' => $user->student_id,
            'access_token' => $token,
            'token_type' => 'Bearer',
        ]);
    }

    public function logout(Request $request)
    {
        $request->user()->currentAccessToken()->delete();

        return response()->json([
            'success' => '1',
            'MSG' => 'Logged out successfully'
        ]);
    }

    public function profile(Request $request)
    {
        return response()->json([
            'success' => '1',
            'user' => $request->user()
        ]);
    }
}
