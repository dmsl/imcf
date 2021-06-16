@extends('layouts.app')

@section('content')
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>

<div class="container">
      <div class="row justify-content-center">
          <div class="col-md-8">
            
              <h1><u>Edit meta-rule</u></h1>

              {!! Form::open(['action' => ['MetarulesController@update', $metarule->id], 'method' => 'POST', 'enctype' => 'multipart/form-data']) !!}

              <div class="form-group">
                {{Form::label('name', 'Description')}}
                {{Form::text('name', $metarule->name , ['class' => 'form-control', 'placeholder' => ''] )}}
              </div>

              <div class="form-group">
                  {{Form::label('action', 'Action')}}
                  <br>
                  <select id="actionOption" onchange="val()" class="browser-default custom-select form-control" name="action" required >
                    <option disabled >Please select an action</option>
                    @if ($metarule->action == 'TEMPER.')
                      <option selected value="TEMPER.">Set temperature level</option>
                      <option value="LIGHT">Set light level</option>
                      <option value="kWh">Set preferred kWh limit</option>
                    @elseif ($metarule->action == 'LIGHT')
                      <option  value="TEMPER.">Set temperature level</option>
                      <option selected value="LIGHT">Set light level</option>
                      <option value="kWh">Set preferred kWh limit</option>
                    @elseif ($metarule->action == 'kWh')
                      <option selected value="TEMPER.">Set temperature level</option>
                      <option value="LIGHT">Set light level</option>
                      <option selected value="kWh">Set preferred kWh limit</option>
                    @endif
                  </select>
              </div>

              <div class="form-group time ">
                {{Form::label('time', 'Time Duration')}}
                <div class="row">
                    <div class="col-md">
                      {{Form::label('timeFrom', 'From:')}}
                      {{Form::time('timeFrom', $metarule->timeFrom, ['class' => 'form-control', 'placeholder' => ''] )}}
                    </div>
                    <div class="col-md">
                      {{Form::label('timeTo', 'To:')}}
                      {{Form::time('timeTo', $metarule->timeTo, ['class' => 'form-control', 'placeholder' => ''] )}}
                    </div>
                </div>
              </div>

              <div class="form-group">
                {{Form::label('value', 'Value')}}
                {{Form::text('value', $metarule->value, ['class' => 'form-control', 'placeholder' => ''] )}}
              </div>



                {{Form::hidden('_method', 'PUT')}}
                {{Form::submit('Submit', ['class' => 'btn btn-primary'] )}}
              {!! Form::close() !!}

    </div>
  </div>
</div>

  <script>

  $( document ).ready(function() {
    var optionType = "{{$metarule->action}}";
    if (optionType.includes("kWh")) {
       $(".time").hide();
    }
    else {
      var times = "{{$metarule->time}}"
      var timeArr = times.split('-');
      $('#timeFrom').val(timeArr[0]);
      $('#timeTo').val(timeArr[1]);
    }
  });

    function val() {
      var e = document.getElementById("actionOption");
      var optionType = e.options[e.selectedIndex].value;
      if (optionType.includes("kWh")) {
         $(".time").hide();
         //$("input[name='time']").attr("disabled", true);
         //$('#time').val('kWh');
      }
      else {
        //$("input[name='time']").attr("disabled", false);
        //$('#time').val('');
        $(".time").show();
      }
    }
  </script>

@endsection
