

<?php $__env->startSection('content'); ?>

  <h1><u>Create meta-rule</u></h1>

  <?php echo Form::open(['action' => 'MetarulesController@store', 'method' => 'POST', 'enctype' => 'multipart/form-data']); ?>


  <div class="form-group">
    <?php echo e(Form::label('name', 'Description')); ?>

    <?php echo e(Form::text('name', '', ['class' => 'form-control', 'placeholder' => ''] )); ?>

  </div>

  <div class="form-group">
      <?php echo e(Form::label('action', 'Action')); ?>

      <br>
      <select id="actionOption" onchange="val()" class="browser-default custom-select form-control" name="action" required >
        <option selected disabled >Please select an action</option>
        <option value="TEMPER.">Set temperature level</option>
        <option value="LIGHT">Set light level</option>
        <option value="kWh">Set preferred kWh limit</option>
      </select>
  </div>

  <div class="form-group time ">
    <?php echo e(Form::label('time', 'Time Duration')); ?>

    <div class="row">
        <div class="col-md">
          <?php echo e(Form::label('timeFrom', 'From:')); ?>

          <?php echo e(Form::time('timeFrom', '', ['class' => 'form-control', 'placeholder' => ''] )); ?>

        </div>
        <div class="col-md">
          <?php echo e(Form::label('timeTo', 'To:')); ?>

          <?php echo e(Form::time('timeTo', '', ['class' => 'form-control', 'placeholder' => ''] )); ?>

        </div>
    </div>
  </div>

  <div class="form-group">
    <?php echo e(Form::label('value', 'Value')); ?>

    <?php echo e(Form::text('value', '', ['class' => 'form-control', 'placeholder' => ''] )); ?>

  </div>

    <?php echo e(Form::submit('Submit', ['class' => 'btn btn-primary'] )); ?>

  <?php echo Form::close(); ?>



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


<?php $__env->stopSection(); ?>

<?php echo $__env->make('layouts.app', \Illuminate\Support\Arr::except(get_defined_vars(), ['__data', '__path']))->render(); ?><?php /**PATH /home/vagrant/Laravel/imcf/resources/views/metarules/create.blade.php ENDPATH**/ ?>