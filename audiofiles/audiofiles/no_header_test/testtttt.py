#!/usr/bin/env python3

# created by chatgpt in python since I just want a quick demo of what errors show when trying to read a wav file without a header

"""
Generate a WAV-like file without a header (raw PCM) and a proper WAV for comparison.
- Writes `no_header.wav` containing raw 16-bit little-endian PCM samples (no RIFF header).
- Writes `proper.wav` which is a valid WAV file with identical audio data.

Run: python generate_no_header_wav.py
"""

import math
import struct
import wave

sample_rate = 44100
duration = 1.0  # seconds
freq = 440.0  # A4
n_samples = int(sample_rate * duration)
amplitude = int(0.5 * (2**15 - 1))

print('Generating', n_samples, 'samples...')

# Create headerless file (raw PCM data only)
with open('no_header.wav', 'wb') as f:
    for i in range(n_samples):
        t = i / sample_rate
        s = int(amplitude * math.sin(2 * math.pi * freq * t))
        f.write(struct.pack('<h', s))  # little-endian signed 16-bit

print('Wrote headerless file: no_header.wav (raw PCM 16-bit LE, 44100 Hz, mono)')

# Create proper WAV file for comparison
with wave.open('proper.wav', 'wb') as wf:
    wf.setnchannels(1)
    wf.setsampwidth(2)  # bytes
    wf.setframerate(sample_rate)
    frames = bytearray()
    for i in range(n_samples):
        t = i / sample_rate
        s = int(amplitude * math.sin(2 * math.pi * freq * t))
        frames += struct.pack('<h', s)
    wf.writeframes(frames)

print('Wrote proper WAV file: proper.wav')