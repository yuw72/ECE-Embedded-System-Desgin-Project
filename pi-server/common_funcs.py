#!/usr/bin/python3

import Adafruit_PCA9685

class ClampControl():
    """
    Wrapper for clamp control.

    https://github.com/adafruit/Adafruit_Python_PCA9685
    """
    servo_min = 300
    servo_max = 550

    servo_close_value2 = 305       # TODO: find correct value
    servo_release_value2 = 190     # TODO: find correct value
    servo_close_value3 = 395       # TODO: find correct value
    servo_release_value3 = 525     # TODO: find correct value
    camera_yaw_low = 550
    camera_yaw_high = 200
    camera_pitch_low = 500
    camera_pitch_high = 300

    def set_servo_pulse(self, channel, pulse):
        pulse_length = 1000000    # 1,000,000 us per second
        pulse_length //= 60       # 60 Hz
        #print('{0}us per period'.format(pulse_length))
        pulse_length //= 4096     # 12 bits of resolution
        #print('{0}us per bit'.format(pulse_length))
        pulse *= 1000
        pulse //= pulse_length
        self.pwm.set_pwm(channel, 0, pulse)

    def _get_valid_value(self, base: int, inc: int) -> int:
        ret_value = base + inc
        if ret_value >= self.servo_min and ret_value <= self.servo_max:
            return ret_value
        else:
            return base

    def __init__(self):
        self.pwm = Adafruit_PCA9685.PCA9685()
        self.pitch_value = 375
        self.yaw_value = 372
        self.clamp2_value = self.servo_close_value2
        self.clamp3_value = self.servo_close_value3

    def release(self):
        " Now useless"
        self.pwm.set_pwm(2, 0, self.servo_release_value2)
        self.pwm.set_pwm(3, 0, self.servo_release_value3)

    def close(self):
        " Now useless"
        self.pwm.set_pwm(2, 0, self.servo_close_value2)
        self.pwm.set_pwm(3, 0, self.servo_close_value3)

    def move(self, inc: int):
        # 2 -, 3 +
        # Close: +; Release: -
        move2 = self.clamp2_value + inc
        if move2 > self.servo_close_value2 or move2 < self.servo_release_value2:
            move2 = self.clamp2_value
        move3 = self.clamp3_value - inc
        if move3 > self.servo_release_value3 or move3 < self.servo_close_value3:
            move3 = self.clamp3_value
        self.pwm.set_pwm(2, 0, move2)
        self.pwm.set_pwm(3, 0, move3)
        self.clamp2_value = move2
        self.clamp3_value = move3

    def pitch(self, inc: int):
        final_value = self._get_valid_value(self.pitch_value, inc)
        self.pwm.set_pwm(0, 0, final_value)
        self.pitch_value = final_value

    def yaw(self, inc: int):
        final_value = self._get_valid_value(self.yaw_value, inc)
        self.pwm.set_pwm(1, 0, final_value)
        self.yaw_value = final_value

    def reset(self):
        self.pwm.set_pwm(0, 0, 372)
        self.pwm.set_pwm(1, 0, 375)
        self.pwm.set_pwm(2, 0, 300)
        self.pwm.set_pwm(3, 0, 375)

    pass

def joystick_to_carmove(angle: int, r: int = 100):
    '''
    :return: (v1, v2), v1, v2: float, 0 -- 100
    '''
    m_v1 = 0.0
    m_v2 = 0.0
    m_max = 100.0

    if angle >= 0 and angle < 90:
        m_v1 = m_max
        m_v2 = m_max * (1 - (2 * (angle / 90.0)))
    elif angle >= 90 and angle < 180:
        m_v1 = m_max * (1 - (2 * ((angle - 90) / 90.0)))
        m_v2 = - m_max
    elif angle >= 180 and angle < 270:
        m_v1 = - m_max
        m_v2 = m_max * (-1 + (2 * ((angle - 180) / 90.0)))
    elif angle >= 270 and angle <= 360:
        m_v1 = m_max * (-1 + (2 * ((angle - 270) / 90.0)))
        m_v2 = m_max
    else:
        raise Exception('angle not in range!')

    return (r * m_v1 / 100.0, r * m_v2 / 100.0)

