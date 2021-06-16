<?php

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;

/*
|--------------------------------------------------------------------------
| API Routes
|--------------------------------------------------------------------------
|
| Here is where you can register API routes for your application. These
| routes are loaded by the RouteServiceProvider within a group which
| is assigned the "api" middleware group. Enjoy building your API!
|
*/

Route::middleware('auth:api')->get('/user', function (Request $request) {
    return $request->user();
});

Route::get('/get-metarules', 'MetarulesController@get_metarules');
Route::post('/update-results', 'MetarulesController@update_results');
Route::get('/get-openhabrules', 'OpenhabrulesController@get_openhabrules');
Route::post('/update-openhabrules', 'OpenhabrulesController@update_openhabrules');