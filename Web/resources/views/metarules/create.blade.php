@extends('layouts.app')

@section('content')
<div class="container">
      <div class="row justify-content-center">
          <div class="col-md-8">

          <h1><u>Create meta-rule</u></h1>

          {!! Form::open(['action' => 'MetarulesController@store', 'method' => 'POST', 'enctype' => 'multipart/form-data']) !!}

          <div class="form-group">
            {{Form::label('name', 'Description')}}
            {{Form::text('name', '', ['class' => 'form-control', 'placeholder' => ''] )}}
          </div>

          <div class="form-group">
              {{Form::label('action', 'Action')}}
              <br>
              <select id="actionOption" onchange="val()" class="browser-default custom-select form-control" name="action" required >
                <option selected disabled >Please select an action</option>
                <option value="TEMPER.">Set temperature level</option>
                <option value="LIGHT">Set light level</option>
                <option value="kWh">Set preferred kWh limit</option>
              </select>
          </div>

          <div class="form-group time ">
            {{Form::label('time', 'Time Duration')}}
            <div class="row">
                <div class="col-md">
                  {{Form::label('timeFrom', 'From:')}}
                  {{Form::time('timeFrom', '', ['class' => 'form-control', 'placeholder' => ''] )}}
                </div>
                <div class="col-md">
                  {{Form::label('timeTo', 'To:')}}
                  {{Form::time('timeTo', '', ['class' => 'form-control', 'placeholder' => ''] )}}
                </div>
            </div>
          </div>

          <div class="form-group">
            {{Form::label('value', 'Value')}}
            {{Form::text('value', '', ['class' => 'form-control', 'placeholder' => ''] )}}
          </div>

            {{Form::submit('Submit', ['class' => 'btn btn-primary'] )}}
          {!! Form::close() !!}
    </div>
  </div>
</div>

  <script>

    function val() {
      var e = document.getElementById("actionOption");
      var optionType = e.options[e.selectedIndex].value;
      if (optionType.includes("kWh")) {
         $(".time").hide();
         //$("input[name='time']").attr("disabled", true);
         //$('#time2').val('kWh');
      }
      else {
        //$("input[name='time']").attr("disabled", false);
        //$('#time').val('');
        $(".time").show();
      }
    }
  </script>


@endsection
