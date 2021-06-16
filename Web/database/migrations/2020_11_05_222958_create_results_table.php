<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

class CreateResultsTable extends Migration
{
    /**
     * Run the migrations.
     *
     * @return void
     */
    public function up()
    {
        Schema::create('results', function (Blueprint $table) {
            $table->id();
            $table->string('noIFTTT_kWh')->nullable();
            $table->string('noIFTTT_totalError')->nullable();
            $table->string('noIFTTT_convenienceError')->nullable();
            $table->string('noIFTTT_budgetError')->nullable();
            $table->string('noIFTTT_standardDeviation')->nullable();
            $table->string('ep_kWh')->nullable();
            $table->string('ep_totalError')->nullable();
            $table->string('ep_convenienceError')->nullable();
            $table->string('ep_budgetError')->nullable();
            $table->string('ep_standardDeviation')->nullable();
            $table->timestamps();
        });
    }

    /**
     * Reverse the migrations.
     *
     * @return void
     */
    public function down()
    {
        Schema::dropIfExists('results');
    }
}
